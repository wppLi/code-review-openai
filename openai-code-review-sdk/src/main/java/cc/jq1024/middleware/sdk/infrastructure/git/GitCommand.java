package cc.jq1024.middleware.sdk.infrastructure.git;

import cc.jq1024.middleware.sdk.types.utils.RandomUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * git命令
 * @author li--jiaqiang
 * @date 2024−08−06
 */
public class GitCommand {

    private static final Logger log = LoggerFactory.getLogger(GitCommand.class);
    /** github 审查日志仓库路径 */
    private final String githubReviewLogUri;
    /** 代码评审的uri */
    private final String githubReturnLogUri;
    /** github密钥 */
    private final String githubToken;
    /** 项目名称 */
    private final String project;
    /** 当前分支 */
    private final String branch;
    /** 提交代码作者 */
    private final String author;
    /** 提交描述信息 */
    private final String message;


    public String getProject() {
        return project;
    }
    public String getBranch() {
        return branch;
    }
    public String getAuthor() {
        return author;
    }
    public String getMessage() {
        return message;
    }


    public GitCommand(String githubReviewLogUri, String githubToken, String project, String branch, String author, String message, String githubReturnLogUri) {
        if (githubReturnLogUri.endsWith("/")) {
            throw new RuntimeException("reviewLogUri can not be 【/】 as end.");
        }
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
        this.githubReturnLogUri = githubReturnLogUri;
    }


    /**
     * 代码检出
     * @return diff code
     */
    public String gitDiff() throws IOException, InterruptedException {
        /*
         * processBuilder = new ProcessBuilder(...);：这行代码创建了一个新的ProcessBuilder实例
         * git"：这是命令的第一个参数，指明了要运行的程序名称。在这个例子中，它调用了Git版本控制系统。
         * "log"：这是Git命令行工具的一个子命令，用于显示提交历史。
         * "-1"：这是git log命令的一个参数，表示只显示最近一次提交。
         * "--pretty=format:%H"：这也是git log命令的一个参数。--pretty选项用于自定义输出的格式。在这里，%H是一个格式占位符，代表提交的完整哈希值。
         *
         * 创建一个进程，该进程将执行git log -1 --pretty=format:%H命令，该命令将输出最近一次提交的完整哈希值。
         */
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        // 进程应该在其下执行的工作目录, .表示当前目录
        logProcessBuilder.directory(new File("."));
        // 启动
        Process logProcess = logProcessBuilder.start();
        // 获取最新提交的 git hash 值
        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = logReader.readLine();
        // 关闭流，释放资源
        logReader.close();
        // 等待上面启动的进程执行完成
        int logExitCode = logProcess.waitFor();
        if (logExitCode != 0) {
            // 异常退出
            throw new RuntimeException("Failed to git git hash, exit code: " + logExitCode);
        }
        // 通过 git hash 确保一定可以获取到两次 git diff 的代码
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        // 读取两次diff不同的代码
        StringBuilder diffCode = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();
        // 等待完成,获取推出码
        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            // 异常退出
            throw new RuntimeException("Failed to git diff, exit code: " + exitCode);
        }
        // 返回diff code
        return diffCode.toString();
    }


    public String commitAndPush(String recommend) throws Exception {
        log.info("githubReviewLogUri: {}  githubToken:{}", githubReviewLogUri, githubToken);
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri + ".git") // 仓库地址
                .setDirectory(new File("repo")) // 设置日志存储路径
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        // 每天的日志放在一个文件夹
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String filePath ="repo/" + dateFolderName;
        File dateFolder = new File(filePath);
        if (!dateFolder.exists()) {
            // 目录不存在，则创建
            boolean mkdir = dateFolder.mkdir();
            log.info("目录【{}】不存在，创建结果：{}", filePath, mkdir);
        }
        // 文件名: 工程名 + 分支 + 作者 + 时间戳 + 随机数 + .md
        String fileName = project + "-" + branch + "-" + author + System.currentTimeMillis() + RandomUtils.generateRandomString(6) + ".md";

        // 提交代码
        File newFile = new File(dateFolder, fileName);
        try(FileWriter writer = new FileWriter(newFile)) {
            writer.write(recommend);
        }
        // git命令进行提交
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add a new log file: " + fileName).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();
        log.info("Changes have been pushed to the repository.");

        // 返回地址
        return githubReturnLogUri + "/" + fileName;
    }
}