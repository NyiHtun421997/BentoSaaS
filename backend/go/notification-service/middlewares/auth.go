package middlewares

import (
	"log"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"nyihtuun.com/bentosystem/utils"
)

func Authenticate(context *gin.Context) {
	raw := context.Request.Header.Get("Authorization")
	if raw == "" {
		context.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	const prefix = "Bearer "
	if !strings.HasPrefix(raw, prefix) {
		context.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	token := strings.TrimSpace(strings.TrimPrefix(raw, prefix))
	if token == "" {
		context.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}

	userId, err := utils.VerifyToken(token)
	if err != nil {
		log.Printf("Error verifying token: %v", err)
		context.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}

	context.Set("userId", userId)
	context.Next()
}
