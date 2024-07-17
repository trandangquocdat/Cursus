//package com.fpt.cursus.util;
//
//import com.fpt.cursus.entity.Account;
//import com.fpt.cursus.enums.Role;
//import com.fpt.cursus.repository.AccountRepo;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.context.ContextConfiguration;
//
//import java.lang.reflect.Field;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@ExtendWith(MockitoExtension.class)
//@ContextConfiguration(classes = {TokenHandler.class,
//        AccountRepo.class})
//class TokenHandlerTest {
//
//    @InjectMocks
//    private TokenHandler tokenHandler;
//
//    @Mock
//    private AccountRepo accountRepo;
//
//    private Account account;
//
//    @BeforeEach
//    void setUp() throws NoSuchFieldException,
//            IllegalAccessException {
//        setField(tokenHandler, "secretKey", "c");
//        setField(tokenHandler, "accessTokenExpiration", 8640);
//        setField(tokenHandler, "refreshTokenExpiration", 6048);
//
//        account = new Account();
//        account.setUsername("john.doe");
//        account.setRole(Role.STUDENT);
//        account.setFullName("John Doe");
//    }
//
//    void setField(Object target, String name, Object value) throws IllegalAccessException,
//            NoSuchFieldException {
//        Field field = target.getClass().getDeclaredField(name);
//        field.setAccessible(true);
//        field.set(target, value);
//    }
//
//    @Test
//    void generateAccessToken() {
//        //then
//        String result = tokenHandler.generateAccessToken(account);
//        assertNotNull(result);
//    }
//
//    @Test
//    void generateRefreshToken() {
//        //then
//        String result = tokenHandler.generateRefreshToken(account);
//        assertNotNull(result);
//    }
//
//    @Test
//    void getInfoByToken() {
//        //given
//        String token = tokenHandler.generateAccessToken(account);
//        //when
//        String result = tokenHandler.getInfoByToken(token);
//        //then
//        assertEquals(account.getUsername(), result);
//    }
//}
