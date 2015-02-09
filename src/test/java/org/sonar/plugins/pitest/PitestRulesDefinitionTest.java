package org.sonar.plugins.pitest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.config.Settings;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

@RunWith(MockitoJUnitRunner.class)
public class PitestRulesDefinitionTest {

    @Mock
    private Settings settings;

    @Mock
    private RulesDefinitionXmlLoader xmlLoader;
    @InjectMocks
    private PitestRulesDefinition pitestRulesDefinition;

    @Test
    public void testDefine() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

}
