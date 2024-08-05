curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiNWIwZTIxMjQ0NGZiYWYxZWQxMWNmOGNmYTkwODliNWYiLCJleHAiOjE3MjI4NTU3MTIxMDQsInRpbWVzdGFtcCI6MTcyMjg1MzkxMjEyMX0.qQM0jj_hiwxjGD68PN3ENWKGtk-ibcvEK-e2UoDkZoE" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions
