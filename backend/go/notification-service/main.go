package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os/signal"
	"strconv"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"nyihtuun.com/bentosystem/config"
	"nyihtuun.com/bentosystem/repository"
	"nyihtuun.com/bentosystem/routes"
	"nyihtuun.com/bentosystem/worker"
)

func main() {
	config.LoadConfig()
	repository.InitNotificationDB()
	defer repository.CloseDB()

	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	go worker.StartConsumer(ctx)

	router := gin.Default()
	routes.RegisterRoutes(router)
	addr := config.Cfg.ServerAddress + ":" + strconv.Itoa(config.Cfg.ServerPort)
	srv := &http.Server{Addr: addr, Handler: router.Handler()}

	serverErr := make(chan error, 1)
	go func() {
		log.Printf("HTTP listening on %s", addr)
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			serverErr <- err
		}
	}()
	select {
	case <-ctx.Done():
		log.Println("shutdown signal received")
	case err := <-serverErr:
		log.Printf("http server error: %v", err)
		stop()
	}

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := srv.Shutdown(shutdownCtx); err != nil {
		log.Printf("server shutdown error: %v", err)
	}

	log.Println("Server exiting")
}
