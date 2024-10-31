from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
import os


download_path = os.getcwd()


if not os.path.exists(download_path):
    os.makedirs(download_path)


options = webdriver.ChromeOptions()
options.add_experimental_option('prefs', {
    "download.default_directory": download_path,  
    "download.prompt_for_download": False,
    "download.directory_upgrade": True,
    "safebrowsing.enabled": True,
    "profile.default_content_setting_values.automatic_downloads": 1
})


driver = webdriver.Chrome(options=options)

try:
    
    driver.get("https://www.microsoft.com/en-us/download/details.aspx?id=35588")

    
    wait = WebDriverWait(driver, 10)
    download_button = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, ".dlcdetail__download-btn.btn.btn-primary")))

    
    download_button.click()

    
    time.sleep(10)  

finally:
    driver.quit()
