package com.oa.mapper;

import com.oa.ServiceAuthApplication;
import com.oa.auth.mapper.SysRoleMapper;
import com.oa.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author NO
 * @create 2023-03-20-10:37
 */

@SpringBootTest(classes = ServiceAuthApplication.class)
public class TestMp {

    @Autowired
    private SysRoleMapper mapper;

    @Test
    public void getAll(){
        List<SysRole> roleList = mapper.selectList(null);

        System.out.println(roleList);
    }


}
