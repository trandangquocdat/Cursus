package com.fpt.cursus.exception.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {

    //User error - 60x
    UNCATEGORIZED_ERROR(999, "Uncategorized error"),
    USER_NOT_FOUND(600, "User not found"),
    USER_EXISTS(601, "User already exists"),
    USER_UNAUTHORIZED(602, "User doesn't have permission to perform this action "),
    USER_ENROLLED_EMPTY(603, "User hasn't enrolled in any courses "),
    USER_ROLE_CAN_NOT_SELECTED(604, "This role can not be selected"),
    REFRESH_TOKEN_NOT_VALID(605, "Refresh token not valid"),
    REFRESH_TOKEN_EXPIRED(606, "Refresh token expired"),
    TOKEN_INVALID(607, "Token invalid"),
    //Register error - 61x
    USERNAME_EXISTS(610, "Username already exists"),
    USERNAME_SIZE_INVALID(611, "Username must be between 4 and 18 characters"),
    USERNAME_NULL(612, "Username can not be null"),
    USERNAME_CONTAINS_WHITESPACE(613, "Username contains whitespace"),
    // Fullname error - 62X
    FULLNAME_INVALID_BLANK(620, "Please don't leave BLANK in Fullname"),
    FULLNAME_NULL(621, "Fullname can not be null"),
    FULLNAME_INVALID_SIZE(622, "Fullname must be less than 200 characters"),
    //Password error - 63x
    PASSWORD_NOT_CORRECT(630, "Password is incorrect"),
    PASSWORD_NOT_MATCH(631, "Password does not match"),
    PASSWORD_IS_SAME_CURRENT(632, "Password is same as current password"),
    PASSWORD_SIZE_INVALID(633, "Password must be between 6 and 18 characters"),
    PASSWORD_NULL(634, "Password can not be null"),
    PASSWORD_CONTAINS_WHITESPACE(635, "Password contains whitespace"),
    //phone error - 64X
    PHONE_NOT_VALID(640, "Phone not valid"),
    PHONE_NULL(641, "Phone can not be null"),
    //email error - 65X
    EMAIL_UNAUTHENTICATED(650, "Please check your email to verify your account"),
    EMAIL_NOT_FOUND(651, "Email not found"),
    EMAIL_INVALID(652, "Email invalid"),
    EMAIL_EXISTS(654, "Email already exists"),
    EMAIL_CAN_NOT_SEND(655, "Email can not send"),
    EMAIL_NULL(656, "Email can not be null"),
    //OTP error - 66X
    OTP_INVALID(660, "Wrong OTP"),
    OTP_EXPIRED(661, "OTP expired"),
    OTP_NOT_FOUND(662, "OTP not found"),
    //
    AVATAR_EMPTY(670, "Avatar can not be empty"),
    //Course error - 70x

    COURSE_NOT_FOUND(700, "Course not found"),
    COURSE_SIZE_INVALID(701, "Course name must be less than 200 characters"),
    COURSE_PRICE_NULL(702, "Price of course can not be null"),
    COURSE_CATEGORY_NULL(703, "Category of course can not be null"),
    COURSE_EXISTS(704, "Course already exists"),
    COURSE_PRICE_INVALID(705, "Price of course must be greater than 10.000 VND and less than 10.000.000 VND"),
    CATEGORY_NOT_FOUND(706, "Category not found"),

    //chapter error - 71x
    CHAPTER_NAME_NULL(710, "Chapter name can not be null"),
    CHAPTER_SIZE_INVALID(712, "Chapter name must be less than 200 characters"),
    CHAPTER_COURSE_ID_NULL(713, "Course id of chapter can not be null"),
    CHAPTER_NOT_FOUND(714, "Chapter not found"),
    //lesson error - 72x
    LESSON_NAME_NULL(720, "Lesson name can not be null"),
    LESSON_SIZE_INVALID(721, "Lesson name must be less than 200 characters"),
    LESSON_VIDEO_NULL(722, "Price of lesson can not be null"),
    LESSON_CHAPTER_ID_NULL(723, "Category of lesson can not be null"),
    LESSON_NOT_FOUND(724, "Lesson not found"),
    //Order error - 73x
    ORDER_NOT_FOUND(730, "Order not found"),
    ORDER_FAIL(731, "Order fail"),
    ORDER_URL_ENCODE_FAIL(732, "Url order encode fail"),
    ORDER_GENERATE_HMAC_FAIL(733, "Generate hmac fail"),
    ORDER_CART_NULL(734, "Cart can not be null"),
    //File error - 74x
    STORAGE_INITIALIZE_FAIL(740, "Storage initialize fail"),
    FILE_NOT_FOUND(741, "File not found"),
    FILE_DOWNLOAD_FAIL(742, "File download fail"),
    FILE_UPLOAD_FAIL(743, "File upload fail"),
    FILE_INVALID_IMAGE(744, "Invalid image type"),
    FILE_INVALID_PDF(745, "File must be PDF"),
    FILE_INVALID_VIDEO(746, "File must be video"),

    //
    PROCESS_CALCULATE_PERCENT_FAIL(750, "Calculate percent fail"),
    PROCESS_ADD_STUDIED_COURSE_FAIL(751, "Add studied course fail"),
    //
    FEEDBACK_NOT_FOUND(760, "Feedback not found"),
    FEEDBACK_INVALID_RATING(761, "Invalid rating"),
    //
    QUIZ_NOT_FOUND(770, "Quiz not found"),
    QUIZ_READ_FAIL(771, "Quiz read fail"),
    DUPLICATE_QUESTION_ID(772, "Cannot have duplicate question id"),
    //
    COURSE_ENROLL_FAIL(780, "Course enroll fail"),
    COURSE_ENROLL_EXISTS(781, "Course already enrolled"),
    COURSE_NOT_ENROLLED(782, "Course not enrolled"),
    //
    INVALID_OFFSET(800, "Invalid Page"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;

    }

}
