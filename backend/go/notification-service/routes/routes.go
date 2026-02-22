package routes

import (
	"github.com/gin-gonic/gin"
	"nyihtuun.com/bentosystem/handlers"
	"nyihtuun.com/bentosystem/middlewares"
)

func RegisterRoutes(server *gin.Engine) {
	authenticated := server.Group("/")
	authenticated.Use(middlewares.Authenticate)
	authenticated.GET("/notifications", handlers.GetNotificationsByUserId)
	authenticated.PUT("/notifications/:id", handlers.MarkNotificationsAsRead)
}
