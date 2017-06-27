package com.automation.remarks.kirk

import org.aeonbits.owner.ConfigFactory
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver

/**
 * Created by sergey on 24.06.17.
 */
class Browser(val driver: WebDriver) {

    companion object {

        private val driverContaner = ThreadLocalDriverContainer()

        fun getDriver(): WebDriver {
            return driverContaner.getDriver()
        }

        fun getDefaultConfig(): BrowserConfig {
            return ConfigFactory.create(BrowserConfig::class.java,
                    System.getProperties())
        }

        fun drive(driver: WebDriver = getDriver(),
                  config: BrowserConfig = getDefaultConfig(),
                  closure: Browser.() -> Unit) {
            if (config.startMaximized()) {
                driver.manage().window().maximize()
            } else {
                val screenSize = config.screenSize()
                driver.manage().window().size = Dimension(screenSize[0], screenSize[1])
            }
            if (config.autoClose()) {
                Runtime.getRuntime().addShutdownHook(object : Thread() {
                    override fun run() = driver.quit()
                })
            }
            Browser(driver).apply(closure)
        }
    }

    fun to(url: String): String {
        driver.get(url)
        return driver.currentUrl
    }

    fun <T : Page> to(pageClass: () -> T): T {
        val page = pageClass()
        page.browser = this
        page.url?.let { to(it) }
        return page
    }

    fun element(cssLocator: String): KElement {
        return element(By.cssSelector(cssLocator))
    }

    fun element(locator: By): KElement {
        return KElement(locator, driver)
    }
}