package uk.gov.hmcts.reform.roleassignment.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentSubset;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfigRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Named
@Singleton
public class JacksonUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JacksonUtils.class);

    private JacksonUtils() {
    }

    private static final Map<String, List<RoleConfigRole>> configuredRoles = new HashMap<>();

    public static final JsonFactory jsonFactory = JsonFactory.builder()
        // Change per-factory setting to prevent use of `String.intern()` on symbols
        .disable(JsonFactory.Feature.INTERN_FIELD_NAMES)
        .build();

    public static final ObjectMapper MAPPER = JsonMapper.builder(jsonFactory)
        .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .build();

    public static final CollectionType listType = MAPPER.getTypeFactory().constructCollectionType(
        ArrayList.class,
        RoleConfigRole.class
    );

    public static List<RoleConfigRole> getConfiguredRoles() {
        return configuredRoles.get("roles");
    }

    public static Map<String, JsonNode> convertValue(Object from) {
        return MAPPER.convertValue(from, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }

    public static JsonNode convertValueJsonNode(Object from) {
        return MAPPER.convertValue(from, JsonNode.class);
    }

    public static TypeReference<HashMap<String, JsonNode>> getHashMapTypeReference() {
        return new TypeReference<HashMap<String, JsonNode>>() {
        };
    }

    //Find Subset for Incoming Records
    public static Set<RoleAssignmentSubset> convertRequestedRolesIntoSubSet(AssignmentRequest assignmentRequest)
        throws InvocationTargetException, IllegalAccessException {
        RoleAssignmentSubset subset;
        Set<RoleAssignmentSubset> roleAssignmentSubsets = new HashSet<>();
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            subset = RoleAssignmentSubset.builder().build();
            BeanUtils.copyProperties(subset, roleAssignment);
            roleAssignmentSubsets.add(subset);
        }

        return roleAssignmentSubsets;

    }

    //Find Subset for Existing  Records
    public static Map<UUID, RoleAssignmentSubset> convertExistingRolesIntoSubSet(AssignmentRequest assignmentRequest)
        throws InvocationTargetException, IllegalAccessException {
        RoleAssignmentSubset subset;
        Map<UUID, RoleAssignmentSubset> roleAssignmentSubsets = new HashMap<>();
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            subset = RoleAssignmentSubset.builder().build();
            BeanUtils.copyProperties(subset, roleAssignment);
            if (roleAssignment.getRoleType().equals(RoleType.CASE)) {
                //Remove the caseType and jurisdiction entries as it was added by application.
                subset.getAttributes().remove("jurisdiction");
                subset.getAttributes().remove("caseType");
            }
            roleAssignmentSubsets.put(roleAssignment.getId(), subset);
        }

        return roleAssignmentSubsets;

    }

    static {
        configuredRoles.put("roles", getRoleConfigs());
    }

    public static List<RoleConfigRole> getRoleConfigs() {
        List<RoleConfigRole> allRoles = null;
        try {
            URI uri = JacksonUtils.class.getClassLoader().getResource(Constants.ROLES_DIR).toURI();
            LOG.debug("Roles absolute dir is {}", uri);

            final String[] array = uri.toString().split("!");
            if (array.length > 1) {
                try (FileSystem fileSystems = FileSystems.newFileSystem(URI.create(array[0]), new HashMap<>())) {
                    Path dirPath = fileSystems.getPath(array[1], Arrays.copyOfRange(array, 2, array.length));
                    allRoles = readFiles(dirPath);
                }
            } else {
                allRoles = readFiles(Paths.get(uri));
            }
        } catch (IOException | URISyntaxException e) {
            LOG.error(e.getMessage());
        }
        assert allRoles != null;

        allRoles.forEach(role -> role.getPatterns().forEach(p -> System.out.println(
            Arrays.toString(p.getRoleType().getValues().toArray()))));
        LOG.info("Loaded {} roles from drool", allRoles.size());
        return allRoles;
    }

    private static List<RoleConfigRole> readFiles(Path dirPath) throws IOException {
        List<RoleConfigRole> allRoles = new ArrayList<>();
        Files.walk(dirPath).filter(Files::isRegularFile).sorted(Comparator.comparing(Path::toString)).forEach(f -> {
            try {
                LOG.debug("Reading role {}", f);
                allRoles.addAll(MAPPER.readValue(Files.newInputStream(f), listType));
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        });
        return allRoles;
    }

}
