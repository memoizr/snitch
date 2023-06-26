import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.io.PrintWriter

fun main() {
    println("hey?")
    val outputPath = "output.kt"
    val rootPath = "core/src/main/kotlin"
    val outputWriter = PrintWriter(outputPath)

    val config = CompilerConfiguration()
    val projectEnvironment = KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(),
        config,
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    )
    val project = projectEnvironment.project
    val psiManager = PsiManager.getInstance(project)

    val root = File(rootPath)
    val ktFiles = root.walk().filter { it.isFile && it.extension == "kt" }.toList()

    for (file in ktFiles) {
        val psiFile = psiManager.findFile(
            projectEnvironment.projectEnvironment.environment.localFileSystem.findFileByIoFile(
                file
            )!!
        ) as KtFile
        val topLevels = extractTopLevels(psiFile)
        topLevels.forEach { outputWriter.println(it.text + "\n") }
    }

    outputWriter.close()
}

fun extractTopLevels(psiFile: KtFile): List<KtDeclaration> {
    val children = psiFile.children
    return children.filterIsInstance<KtDeclaration>()
}