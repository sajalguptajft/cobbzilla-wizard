package org.cobbzilla.wizard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.util.security.bcrypt.BCrypt;
import org.cobbzilla.util.security.bcrypt.BCryptUtil;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import java.io.Serializable;

import static org.cobbzilla.util.daemon.ZillaRuntime.*;
import static org.cobbzilla.util.string.StringUtil.truncate;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.*;

@Embeddable @NoArgsConstructor @Slf4j
public class HashedPassword implements Serializable {

    public static final HashedPassword DISABLED = new HashedPassword(true, "disabled");
    public static final HashedPassword DELETED = new HashedPassword(true, "deleted");

    public static final String SPECIAL_PREFIX = ":special:";
    public static final String SPECIAL_SUFFIX = "__special__";

    public HashedPassword (String password) { setPassword(password); }

    private HashedPassword (boolean special, String val) { hashedPassword = SPECIAL_PREFIX +truncate(val, 10)+SPECIAL_SUFFIX; }

    @JsonIgnore @Transient public boolean isSpecial () {
        return hashedPassword.startsWith(SPECIAL_PREFIX) && hashedPassword.endsWith(SPECIAL_SUFFIX);
    }
    @JsonIgnore @Transient public boolean isNotSpecial () { return !isSpecial(); }

    @HasValue(message=ERR_HASHED_PASSWORD_EMPTY)
    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_HASHED_PASSWORD_LENGTH)
    @Column(nullable=false, length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String hashedPassword;
    @JsonIgnore public boolean hasPassword () { return !empty(hashedPassword); }

    @Size(min=RESETTOKEN_MAXLEN, max=RESETTOKEN_MAXLEN, message=ERR_RESET_TOKEN_LENGTH)
    @Column(length=RESETTOKEN_MAXLEN)
    private String resetToken;
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
        this.resetTokenCtime = (resetToken == null) ? null : now();
    }

    public String initResetToken() {
        final String token = RandomStringUtils.randomAlphanumeric(BasicConstraintConstants.RESETTOKEN_MAXLEN);
        setResetToken(token);
        return token;
    }

    @Getter @Setter private Long resetTokenCtime;

    @Transient @JsonIgnore
    public long getResetTokenAge () { return resetTokenCtime == null ? 0 : now() - resetTokenCtime; }

    @Transient
    public boolean isCorrectPassword (String password) {
        try {
            return password != null && BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            log.warn("isCorrectPassword: "+shortError(e));
            return false;
        }
    }

    public void setPassword(String password) { this.hashedPassword = BCryptUtil.hash(password); }

    public void resetPassword(String password, long tokenDuration) {
        if (getResetTokenAge() > tokenDuration) die("token expired");
        setPassword(password);
    }

}
