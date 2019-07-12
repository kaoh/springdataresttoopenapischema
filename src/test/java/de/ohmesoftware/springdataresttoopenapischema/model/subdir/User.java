package de.ohmesoftware.springdataresttoopenapischema.model.subdir;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A user being able to log-in.
 * <p>
 *     More details.
 * </p>
 *
 * @author <a href="mailto:k_o_@users.sourceforge.net">Karsten Ohme
 * (k_o_@users.sourceforge.net)</a>
 */
@Searchable(value = {"username", "emailAddress"})
public class User extends Base {

    /**
     * The username.
     */
    public String username;

    /**
     * The email address.
     * <p>
     *     Escape "test"
     * </p>
     */
    @io.swagger.v3.oas.annotations.media.Schema(description = "Test")
    public String emailAddress;

    /**
     * The password.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String password;

    /**
     * The role.
     */
    public String role;

    public String firstName;

    public String lastName;

    public boolean blocked;

    public int failedLoginAttempts;

    @JsonIgnore
    public String extra;

    public Organisation organisation;
}
