package org.gark87.idea.regexp.nazi.test;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.QuickFix;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.InspectionFixtureTestCase;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.gark87.idea.regexp.nazi.inspections.EmptyChoice;
import org.intellij.lang.regexp.RegExpFileType;

import java.io.IOException;

/**
 * This test is about {@link EmptyChoice}
 *
 * @author gark87 <a href="mailto:my_another@mail.ru">my_another&064;mail.ru</a>
 */
public abstract class RegExpNaziTest extends InspectionFixtureTestCase {
    private CodeInsightTestFixture myFixture;

    public RegExpNaziTest() {
        System.setProperty("idea.platform.prefix", "Idea");
        PlatformTestCase.initPlatformLangPrefix();
    }

    protected abstract LocalInspectionTool createInspectionToTest();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
        final TestFixtureBuilder<IdeaProjectTestFixture> builder = fixtureFactory.createFixtureBuilder();
        myFixture = fixtureFactory.createCodeInsightFixture(builder.getFixture());
        myFixture.setTestDataPath("testData");
        myFixture.setUp();
        final Project project = myFixture.getProject();
        new WriteCommandAction.Simple(project) {
            @Override
            protected void run() throws Throwable {
                FileTypeManager.getInstance().registerFileType(RegExpFileType.INSTANCE, "regexp");
            }
        }.execute();
    }

    protected void doTestNoProblems(String regexp) {
        Project project = myFixture.getProject();
        InspectionManager instance = InspectionManager.getInstance(project);
        LocalInspectionTool inspectionToTest = createInspectionToTest();
        PsiFile file = myFixture.configureByText(RegExpFileType.INSTANCE, regexp);
        ProblemDescriptor[] problemDescriptors = inspectionToTest.checkFile(file, instance, true);
        assertEmpty(problemDescriptors);
    }

     protected void doTestAllRegexFix(String regExp, String... afterFixes) throws IOException {
         doTestFixes("", regExp, "", afterFixes);
     }

    protected void doTestFixes(String regExpBeforeError, String error, String regExpAfterError,
                               final String... afterFixes) throws IOException
    {
        String regExp = regExpBeforeError + error + regExpAfterError;
        final Project project = myFixture.getProject();
        InspectionManager instance = InspectionManager.getInstance(project);
        LocalInspectionTool inspectionToTest = createInspectionToTest();
        final PsiFile file = myFixture.configureByText(RegExpFileType.INSTANCE, regExp);
        ProblemDescriptor[] problemDescriptors = inspectionToTest.checkFile(file, instance, true);
        if (problemDescriptors.length == 0)
            assertTrue("no problems was found for `" + regExp + "`", problemDescriptors.length > 0);
        final ProblemDescriptor pd = problemDescriptors[0];
        String problem = pd.getPsiElement().getText();
        assertEquals("Wrong error text", error, problem);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                int length = afterFixes.length;
                QuickFix[] fixes = pd.getFixes();
                for (int i = 0; i < length; i++) {
                    fixes[i].applyFix(project, pd);
                    assertEquals("fixed code differs", afterFixes[i], file.getText());
                }
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        myFixture.tearDown();
        myFixture = null;
        super.tearDown();
    }

}
