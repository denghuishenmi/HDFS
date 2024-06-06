import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import utils.HDFSUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class HDFSFileManager extends Application {

    private TreeItem<String> rootItem;
    private Button uploadButton;
    private Button downloadButton;
    private Button deleteButton;
    private Button renameButton;
    private Button createDirButton;
    private static String hdfsURL = "hdfs://localhost:9000";
    private static Configuration conf;

    static {
        conf = new Configuration();
        conf.set("fs.defaultFS", hdfsURL);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("我的网盘");


        // 创建按钮
        uploadButton = new Button("上传");
        downloadButton = new Button("下载");
        deleteButton = new Button("删除");
        renameButton = new Button("修改名称");
        createDirButton = new Button("创建文件夹");
        downloadButton.setDisable(true); // 默认下载按钮不可用
        deleteButton.setDisable(true); // 默认删除按钮不可用
        renameButton.setDisable(true); // 默认修改名称按钮不可用

        // 创建文件树视图
        rootItem = new TreeItem<>("根目录");
        rootItem.setExpanded(true); // 默认展开根节点
        TreeView<String> treeView = new TreeView<>(rootItem);

        // 加载文件树
        loadFileTree(rootItem, "/");

        // 将按钮和树视图添加到布局中
        // 创建按钮容器
        HBox buttonBox = new HBox(5); // 按钮间距为 5
        buttonBox.getChildren().addAll(uploadButton, downloadButton, deleteButton, renameButton, createDirButton);
        // 创建主布局
        BorderPane root = new BorderPane();
        root.setTop(buttonBox);  // 按钮栏在顶部
        root.setCenter(treeView);  // 文件树在中心

        // 按钮点击事件
        uploadButton.setOnAction(event -> {
            // 获取当前选中的文件夹路径
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            String hdfsDirectory;
            if (selectedItem != null && !selectedItem.getValue().equals("根目录")) {
                hdfsDirectory = getHdfsFilePath(selectedItem);
            } else {
                hdfsDirectory = "/";
            }

            // 弹出文件选择对话框，选择本地文件
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择上传文件");
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                String localFilePath = selectedFile.getAbsolutePath();
                try {
                    HDFSUtil.uploadToHDFS(localFilePath, hdfsDirectory);
                    System.out.println("文件上传成功！");
                    refreshTreeView(); // 上传成功后刷新文件树的视图
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("文件上传失败：" + e.getMessage());
                }
            }
        });
        // 下载指定文件
        downloadButton.setOnAction(event -> {
            // 获取选中的文件树节点
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.isLeaf()) {
                String selectedFileName = selectedItem.getValue();
                String hdfsFilePath = getHdfsFilePath(selectedItem);
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("选择下载目录");
                // 设置初始文件名
                fileChooser.setInitialFileName(selectedFileName);
                // 设置扩展名过滤器
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFileDirectory = fileChooser.showSaveDialog(primaryStage);
                if (selectedFileDirectory != null) {
                    String localDirectory = selectedFileDirectory.getAbsolutePath();
                    try {
                        HDFSUtil.downloadFromHDFS(hdfsFilePath, localDirectory);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("文件下载失败：" + e.getMessage());
                    }
                }
            }
        });
        // 删除文件或文件夹
        deleteButton.setOnAction(event -> {
            // 获取选中的文件树节点
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String hdfsFilePath = getHdfsFilePath(selectedItem);
                try {
                    HDFSUtil.deleteFromHDFS(hdfsFilePath);

                    refreshTreeView(); // 删除成功后刷新文件树的视图
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("文件删除失败：" + e.getMessage());
                }
            }
        });
        // 修改文件或文件夹名称
        renameButton.setOnAction(event -> {
            // 获取选中的文件树节点
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                TextInputDialog dialog = new TextInputDialog(selectedItem.getValue());
                dialog.setTitle("修改名称");
                dialog.setHeaderText(null);
                dialog.setContentText("请输入新名称：");

                // 获取新的文件名
                dialog.showAndWait().ifPresent(newName -> {
                    String hdfsFilePath = getHdfsFilePath(selectedItem);
                    int lastIndex = hdfsFilePath.lastIndexOf("/");
                    int secondLastIndex = hdfsFilePath.lastIndexOf("/", lastIndex - 1);
                    // 使用倒数第二个 / 的位置来切分字符串
                    String firstPart = hdfsFilePath.substring(0, secondLastIndex + 1);
                    try {
                        HDFSUtil.renameFromHDFS(hdfsFilePath.substring(0, lastIndex), firstPart.substring(0, secondLastIndex + 1) + newName);
                        refreshTreeView(); // 重命名成功后刷新文件树的视图
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        // 创建文件夹
        createDirButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("创建文件夹");
            dialog.setHeaderText(null);
            dialog.setContentText("请输入文件夹名称：");

            // 获取新文件夹名称
            dialog.showAndWait().ifPresent(dirName -> {
                // 获取当前选中的文件夹路径
                TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
                String hdfsDir = "/";
                if (selectedItem != null && !selectedItem.getValue().equals("根目录")) {
                    hdfsDir = getHdfsFilePath(selectedItem);
                }

                // 拼接新文件夹路径
                String newDirPath = hdfsDir + dirName + "/";
                try {
                    HDFSUtil.createDirFromHDFS(newDirPath);
                    System.out.println("文件夹创建成功！");
                    refreshTreeView(); // 创建文件夹成功后刷新文件树的视图
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("文件夹创建失败：" + e.getMessage());
                }
            });
        });

        // 监听文件树视图的选中事件，更新下载按钮、删除按钮和修改名称按钮的可用状态
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // 如果新选中项不为空
            if (newValue != null) {
                boolean isRoot = newValue.getParent() == null; // 检查是否为根节点
                boolean isLeaf = newValue.isLeaf(); // 检查是否为叶子节点 叶子节点是文件节点
                downloadButton.setDisable(!isLeaf); // 仅当选中的是文件时启用下载按钮
                deleteButton.setDisable(isRoot); // 如果选中的是根节点，则禁用删除按钮
                renameButton.setDisable(isRoot); // 如果选中的是根节点，则禁用重命名按钮
//                downloadButton.setDisable(isRoot); // 如果选中的是根节点，则禁用下载按钮
            } else {
                downloadButton.setDisable(true); // 如果没有选中任何项，禁用所有按钮
                deleteButton.setDisable(true);
                renameButton.setDisable(true);
            }
        });
        // 创建场景并显示
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshTreeView() {
        // 清空文件树
        rootItem.getChildren().clear();
        // 重新加载文件树
        loadFileTree(rootItem, "/");
    }

    private void loadFileTree(TreeItem<String> parentItem, String hdfsDir) {
        try {
            FileSystem fs = FileSystem.get(URI.create(hdfsURL), conf);
            Path dirPath = new Path(hdfsDir);

            if (!fs.exists(dirPath)) {
                System.err.println("当前路径不存在: " + dirPath);
                return;
            }

            FileStatus[] fileStatuses = fs.listStatus(dirPath);

            for (FileStatus fileStatus : fileStatuses) {
                String name = fileStatus.getPath().getName();
                TreeItem<String> item = new TreeItem<>(name);
                parentItem.getChildren().add(item);
                // 获取指定路径下的所有文件和目录的状态，并递归加载子目录
                if (fileStatus.isDirectory()) {
                    loadFileTree(item, fileStatus.getPath().toString());
                }
            }
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("加载文件树失败：" + e.getMessage());
        }
    }

    private String getHdfsFilePath(TreeItem<String> selectedItem) {
        StringBuilder hdfsFilePath = new StringBuilder("/");
        TreeItem<String> currentItem = selectedItem;
        while (currentItem != null) {
            if (currentItem.getValue().equals("根目录")) {
                break;
            }
            hdfsFilePath.insert(0, "/" + currentItem.getValue());
            currentItem = currentItem.getParent();
        }
        return hdfsFilePath.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
