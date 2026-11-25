package tr.edu.sakarya.bil002.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber koşturucusu: src/test/resources/features altındaki bütün .feature dosyalarını
 * JUnit 5 üzerinden çalıştırır. Gherkin etiketleri (@hata) JUnit etiketine dönüşür, bu yüzden
 * {@code -DexcludedGroups=hata} ve {@code -Dgroups=hata} senaryolar için de çalışır.
 * Adım tanımları (glue): tr.edu.sakarya.bil002.bdd paketi. HTML rapor: target/cucumber/rapor.html
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "tr.edu.sakarya.bil002.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber/rapor.html")
class CucumberCalistiriciTesti {
}
