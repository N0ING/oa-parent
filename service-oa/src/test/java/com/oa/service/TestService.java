package com.oa.service;

import com.oa.ServiceAuthApplication;
import com.oa.auth.service.SysRoleService;
import com.oa.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author NO
 * @create 2023-03-20-11:58
 */
@SpringBootTest(classes = ServiceAuthApplication.class)
public class TestService {

    @Autowired
    private SysRoleService service;

    @Test
    public void testService(){
        SysRole role = service.getById(1);
        System.out.println(role);
    }
}
