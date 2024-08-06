package cc.jq1024.middleware.sdk.infrastructure.weixin.dto;

import java.util.HashMap;
import java.util.Map;

public class TemplateMessageDTO {

    private String touser;
    private String template_id;
    private String url = "https://github.com/wppLi/opena-code-review-log/blob/main/2024-08-06/tusYcGvBphNE.md";
    private Map<String, Map<String, String>> data = new HashMap<>();

    public TemplateMessageDTO(String touser, String template_id) {
        this.touser = touser;
        this.template_id = template_id;
    }

    public void put(String key, String value) {
        data.put(key,
                new HashMap<String, String>() {
                    private static final long serialVersionUID = 7092338402387318563L;
                    {
                        put("value", value);
                    }
                });
    }

    public static void put(Map<String, Map<String, String>> data, TemplateKey key, String value){
        data.put(key.getCode(), new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;
            {
                put("value", value);
            }
        });
    }


    public enum TemplateKey {
        REPO_NAME("repo_name","项目名称"),
        BRANCH_NAME("branch_name","分支名称"),
        COMMIT_AUTHOR("commit_author","提交者"),
        COMMIT_MESSAGE("commit_message","提交信息"),
        ;

        private final String code;
        private final String desc;

        TemplateKey(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }


    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplate_id() {
        return template_id;
    }

    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, String>> data) {
        this.data = data;
    }

}