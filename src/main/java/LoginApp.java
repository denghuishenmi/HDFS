import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

// 登录主界面
public class LoginApp extends Application {
    private static String USER_NAME = "admin";
    private static String PASSWORD = "admin";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("登录");

        // 创建登录界面组件
        Label usernameLabel = new Label("用户名:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("密码:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("登录");

        // 设置登录按钮的动作
        loginButton.setOnAction(event -> handleLogin(primaryStage, usernameField, passwordField));
        // 设置密码框按下回车键的动作
        passwordField.setOnAction(event -> handleLogin(primaryStage, usernameField, passwordField));

        // 创建登录界面的布局
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(loginButton, 1, 2);

        // 创建场景并显示
        Scene scene = new Scene(gridPane, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean validateLogin(String username, String password) {
        return USER_NAME.equals(username) && PASSWORD.equals(password);
    }

    private void handleLogin(Stage primaryStage, TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        // 在这里可以添加登录验证逻辑
        if (validateLogin(username, password)) {
            // 登录成功，显示主界面
            HDFSFileManager fileManager = new HDFSFileManager();
            try {
                fileManager.start(new Stage());
                primaryStage.close(); // 关闭登录界面
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 登录失败，可以在这里显示错误消息
            System.out.println("登录失败");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
