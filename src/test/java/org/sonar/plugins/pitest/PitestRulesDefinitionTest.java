/*
 * Sonar Pitest Plugin
 * Copyright (C) 2015 SonarCommunity
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

@RunWith(MockitoJUnitRunner.class)
public class PitestRulesDefinitionTest {

    @Mock
    private Settings settings;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NewRepository newRepository;

    @Mock
    private Context context;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NewRule rule;

    @Mock
    private RulesDefinitionXmlLoader xmlLoader;

    @InjectMocks
    private PitestRulesDefinition subject;

    @Before
    public void setUp() throws Exception {

        setupRule(rule);

    }

    /**
     * Sets up the rule by returning the rule itself on every setter to allow proper builder pattern usage
     *
     * @param rule
     */
    private void setupRule(final NewRule rule) {

        doReturn(rule).when(rule).setDebtRemediationFunction(any(DebtRemediationFunction.class));
        doReturn(rule).when(rule).setDebtSubCharacteristic(anyString());
        doReturn(rule).when(rule).setEffortToFixDescription(anyString());
        doReturn(rule).when(rule).setHtmlDescription(anyString());
        doReturn(rule).when(rule).setHtmlDescription(any(URL.class));
        doReturn(rule).when(rule).setInternalKey(anyString());
        doReturn(rule).when(rule).setMarkdownDescription(anyString());
        doReturn(rule).when(rule).setMarkdownDescription(any(URL.class));
        doReturn(rule).when(rule).setName(anyString());
        doReturn(rule).when(rule).setSeverity(anyString());
        doReturn(rule).when(rule).setStatus(any(RuleStatus.class));
        doReturn(rule).when(rule).setTags(Matchers.<String[]> anyVararg());
        doReturn(rule).when(rule).setTemplate(anyBoolean());
    }

    @Test
    public void testDefine() throws Exception {

        // prepare
        when(context.createRepository("pitest", "java")).thenReturn(newRepository);
        when(newRepository.setName(anyString())).thenReturn(newRepository);
        final List<NewRule> rules = new ArrayList<>();
        when(newRepository.createRule(anyString())).then(new Answer<NewRule>() {

            @Override
            public NewRule answer(final InvocationOnMock invocation) throws Throwable {

                rules.add(rule);
                return rule;
            }

        });
        when(newRepository.rules()).thenReturn(rules);

        // act
        subject.define(context);

        // assert
        verify(newRepository).setName("Pitest");
        verifyXmlLoader();
        verifyRules(rules);
        verify(newRepository).done();
    }

    private void verifyRules(final List<NewRule> rules) {

        // 17 Mutator Rules, see mutator-def.xml
        verify(newRepository, times(17)).createRule(anyString());
        assertEquals(17, rules.size());

        // as we reuse the same rule, we count the total method invocation for each "rule"
        verify(rule, times(17)).setDebtRemediationFunction(any(DebtRemediationFunction.class));
        verify(rule, times(17)).setEffortToFixDescription(anyString());
        verify(rule, times(17)).setDebtSubCharacteristic("UNIT_TESTS");
        // 5 experimental mutators = beta rules
        verify(rule, times(5)).setStatus(RuleStatus.BETA);

    }

    private void verifyXmlLoader() {

        final ArgumentCaptor<NewRepository> captorForRepo = forClass(NewRepository.class);
        final ArgumentCaptor<InputStream> captorForStream = forClass(InputStream.class);
        final ArgumentCaptor<String> captorForEncoding = forClass(String.class);
        verify(xmlLoader).load(captorForRepo.capture(), captorForStream.capture(), captorForEncoding.capture());
        assertEquals(newRepository, captorForRepo.getValue());
        assertEquals("UTF-8", captorForEncoding.getValue());
        assertNotNull(captorForStream.getValue());
    }

}
