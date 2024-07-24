package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.response.InstructorDashboardRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.OrdersDetail;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.DashboardService;
import com.fpt.cursus.service.OrderService;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final AccountUtil accountUtil;
    private final CourseService courseService;
    private final OrderService orderService;
    private final AccountService accountService;

    public DashboardServiceImpl(AccountUtil accountUtil,
                                CourseService courseService,
                                OrderService orderService,
                                AccountService accountService) {
        this.accountUtil = accountUtil;
        this.courseService = courseService;
        this.orderService = orderService;
        this.accountService = accountService;
    }

    @Override
    public InstructorDashboardRes getInstructorDashboardRes() {
        InstructorDashboardRes res = new InstructorDashboardRes();
        Account account = accountUtil.getCurrentAccount();
        List<Long> courseIds = courseService.
                getCourseByCreatedBy(account.getUsername()).stream().map(Course::getId).toList();
        List<OrdersDetail> ordersDetails = orderService.findAllByIdIn(courseIds);
        Double totalSales = 0.0;
        long totalStudents = 0L;
        long totalEnroll = 0L;
        List<String> studentNames = new ArrayList<>();
        for (OrdersDetail ordersDetail : ordersDetails) {
            totalSales += ordersDetail.getPrice();
            totalStudents += 1;
            studentNames.add(ordersDetail.getOrders().getCreatedBy());
        }
        for (String studentName : studentNames) {
            Account student = accountService.getAccountByUsername(studentName);
            List<StudiedCourse> studiedCourses = courseService.getStudiedCourses(student);
            List<Long> courseIds2 = studiedCourses.stream().map(StudiedCourse::getCourseId).toList();
            for (Long courseId : courseIds2) {
                if (!courseIds.contains(courseId)) {
                    totalEnroll += 1;
                }
            }
        }
        res.setCurrentSubscribers(accountService.getSubscribersUsers(account).size());
        res.setTotalCourses(courseIds.size());
        res.setTotalSales(totalSales);
        res.setTotalEnroll(totalEnroll);
        res.setTotalStudents(totalStudents);

        return res;
    }

}
