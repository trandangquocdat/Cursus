package com.fpt.cursus.util;

import com.fpt.cursus.entity.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AccountUtil.class)
class AccountUtilTest {

    @InjectMocks
    private AccountUtil accountUtil;

    @Mock
    private Authentication authentication;

    @Test
    void getCurrentAccount() {
        //given
        Account account = new Account();
        account.setUsername("test");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        when(authentication.getPrincipal()).thenReturn(account);
        //then
        Account result = accountUtil.getCurrentAccount();
        assertEquals(account, result);
    }

}
