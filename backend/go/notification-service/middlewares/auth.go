package middlewares

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"nyihtuun.com/bentosystem/utils"
)

func Authenticate(context *gin.Context) {
	rawToken := context.Request.Header.Get("Authorization")
	token := rawToken[7:]

	if token == "" {
		context.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}

	userId, err := utils.VerifyToken(token)
	if err != nil {
		context.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}

	context.Set("userId", userId)
	context.Next()
}
