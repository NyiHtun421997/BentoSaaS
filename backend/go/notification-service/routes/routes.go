package routes

import (
	"github.com/gin-gonic/gin"
	"nyihtuun.com/bentosystem/handlers"
	"nyihtuun.com/bentosystem/middlewares"
)

func RegisterRoutes(server *gin.Engine) {
	authenticated := server.Group("/")
	authenticated.Use(middlewares.Authenticate)
	authenticated.GET("/notification/v1", handlers.GetNotificationsByUserId)
	authenticated.PUT("/notification/v1/:id", handlers.MarkNotificationsAsRead)
}
