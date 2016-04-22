package com.sheryar.courses;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * The Servlet gets executed when an http request is sent
 * from client and the servlet uses selenium to parse through
 * the website and returns a list of courses the student is
 * enrolled in. PhantomJS plugin is used to make a headless
 * browser.
 */
public class CourseServlet extends HttpServlet {

    public static String currentSemester = "";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("Start running selenium");
        response.setContentType("text/html");

        String name = request.getParameter("userName");
        String pass = request.getParameter("password");

        //This part will divide up the year into 4 semesters based on the months,
        //so the program only gets courses from that semester
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
        String today = formatter.format(new java.util.Date());
        String month = today.substring(4);
        int mon = Integer.parseInt(month);

        if (mon == 1) {
            currentSemester = "Winter ".concat(today.substring(0, 4));
        } else if (mon >= 1 && mon <= 5) {
            currentSemester = "Spring ".concat(today.substring(0, 4));
        } else if (mon >= 6 && mon <= 8) {
            currentSemester = "Summer ".concat(today.substring(0, 4));
        } else if (mon >= 8 && mon <= 12) {
            currentSemester = "Fall ".concat(today.substring(0, 4));
        }


        //Start running selenium with PhantomJS Plugin and start parsing
        //through the site
        DesiredCapabilities DesireCaps = new DesiredCapabilities();
        DesireCaps.setCapability("takesScreenshot", false);
        DesireCaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "E:\\Documents\\StonyBrook University\\ESE & CSE CLASSES\\ESE 441\\phantomjs.exe");
        WebDriver driver = new PhantomJSDriver(DesireCaps);

        System.out.println("Executing PhantomJS plugin");

        driver.get("https://blackboard.stonybrook.edu");
        System.out.println("Successfully accessed blackboard site");

        //Start sending the credentials and click submit
        WebElement myDynamicElement = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("user_id")));
        driver.findElement(By.id("user_id")).sendKeys(name);
        driver.findElement(By.id("password")).sendKeys(pass);
        driver.findElement(By.className("button-1")).click();

        //Checking of incorrect credentials
        if (driver.getPageSource().contains("Please try again.")) {
            response.getWriter().println("Wrong Credentials");
        }

        //This gets the student name from the site
        String StudentName = driver.findElement(By.cssSelector("#global-nav-link")).getText();
        StudentName = StudentName.replaceAll("[^A-Z a-z]", " ");
        response.getWriter().println(StudentName);

        //Hardcoded for now to go to courses page
        driver.navigate().to("https://blackboard.stonybrook.edu//webapps/portal/execute/tabs/tabAction?tab_tab_group_id=_5_1&forwardUrl=edit_module%2F_695_1%2Fbbcourseorg%3Fcmd%3Dedit&recallUrl=%2Fwebapps%2Fportal%2Fexecute%2Ftabs%2FtabAction%3Ftab_tab_group_id%3D_5_1");

        ArrayList<WebElement> allCourses = (ArrayList<WebElement>) driver.findElements(By.tagName("strong"));
        ArrayList<String> allCoursesString = new ArrayList<>();

        //Get the list of all the courses the student is currently enrolled in
        //and store it in a list
        for (int i = 0; i < allCourses.size(); i++) {
            if (allCourses.get(i).getText().contains(currentSemester)) {
                allCoursesString.add(allCourses.get(i).getText());
            }
        }

        //Parse the courses data in order to get it in correct format
        ArrayList<String> currentTempCourse = new ArrayList<>();
        ArrayList<String> currentCourses = new ArrayList<>();
        String result;
        for (int i = 0; i < allCoursesString.size(); i++) {

            result = allCoursesString.get(i).replaceAll("[\\-]", "");
            currentTempCourse.addAll(Arrays.asList(result.split(":")));
            currentTempCourse.remove(i);
            if (currentTempCourse.get(i).contains(currentSemester)) {
                String replaceAll = currentTempCourse.get(i).replaceAll("  " + currentSemester, "");
                currentCourses.add(replaceAll);
            }

        }

        //Start printing out the results
        for (int i = 0; i < currentCourses.size(); i++) {
            response.getWriter().println(currentCourses.get(i));
        }

        System.out.println("Course Retrieval Program Ended");
        driver.quit();

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }

}
