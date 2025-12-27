package com.example.task.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskAppE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // trochę dłuższy timeout na logowanie
    }

    @Test
    public void shouldLoginAndLoadTaskListPageAndFindAddButton() {
        // 1. Wejdź na stronę zadań – zostaniesz przekierowany na login
        driver.get("http://localhost:" + port + "/tasks");

        // 2. Poczekaj na stronę logowania
        wait.until(ExpectedConditions.titleContains("Login")); // lub "Please sign in" jeśli nie masz polskiego tytułu

        // 3. Wypełnij formularz logowania
        WebElement usernameField = driver.findElement(By.name("username")); // domyślne pole w Spring Security
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']")); // lub input[type='submit']

        usernameField.sendKeys("admin");
        passwordField.sendKeys("admin123");
        loginButton.click();

        // 4. Poczekaj na przekierowanie na listę zadań
        wait.until(ExpectedConditions.titleContains("Lista zadań"));

        assertTrue(driver.getTitle().contains("Lista zadań"),
                "Po zalogowaniu tytuł strony powinien zawierać 'Lista zadań'");

        // 5. Sprawdź przycisk dodawania zadania
        By addButtonLocator = By.partialLinkText("Nowe zadanie");
        WebElement addButton = wait.until(ExpectedConditions.visibilityOfElementLocated(addButtonLocator));

        assertTrue(addButton.isDisplayed(), "Przycisk '+ Nowe zadanie' powinien być widoczny");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}