package io.metersphere.ldap.controller;

import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.service.LdapService;
import io.metersphere.ldap.domain.LdapInfo;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/ldap")
public class LdapController {

    @Resource
    private UserService userService;
    @Resource
    private LdapService ldapService;
    @Resource
    private SystemParameterService systemParameterService;

    @PostMapping(value = "/signin")
    public ResultHolder login(@RequestBody LoginRequest request) {

        String isOpen = systemParameterService.getValue(ParamConstants.LDAP.OPEN.getValue());
        if (StringUtils.isBlank(isOpen) || StringUtils.equals(Boolean.FALSE.toString(), isOpen)) {
            MSException.throwException(Translator.get("ldap_authentication_not_enabled"));
        }

        ldapService.authenticate(request);

        SecurityUtils.getSubject().getSession().setAttribute("authenticate", "ldap");

        String username = request.getUsername();
        String password = request.getPassword();

        User u = userService.selectUser(request.getUsername());
        if (u == null) {
            User user = new User();
            user.setId(username);
            user.setName(username);
            // todo user email ?
            user.setEmail(username + "@fit2cloud.com");
            user.setPassword(password);
            userService.createUser(user);
        } else {
            request.setUsername(u.getId());
            request.setPassword(u.getPassword());
        }

        return userService.login(request);
    }

    @PostMapping("/test/connect")
    public void testConnect(@RequestBody LdapInfo ldapInfo) {
        ldapService.testConnect(ldapInfo);
    }

    @PostMapping("/test/login")
    public void testLogin(@RequestBody LoginRequest request) {
        ldapService.authenticate(request);
    }

}
