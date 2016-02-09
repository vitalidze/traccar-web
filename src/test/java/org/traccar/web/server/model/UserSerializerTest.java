package org.traccar.web.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.traccar.web.shared.model.User;

import java.io.IOException;

public class UserSerializerTest {
    ObjectMapper jackson = JacksonUtils.create();

    @Test
    public void testPassword() throws IOException {
        User user = new User();
        JsonNode userJSON = jackson.readTree(jackson.writeValueAsString(user));
        assertFalse(userJSON.has("password"));

        user.setPassword("P");
        userJSON = jackson.readTree(jackson.writeValueAsString(user));
        assertFalse(userJSON.has("password"));

        user = jackson.readValue("{\"password\":\"PWORD\"}", User.class);
        assertEquals("PWORD", user.getPassword());
    }
}
