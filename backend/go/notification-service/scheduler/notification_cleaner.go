package scheduler

import (
	"context"
	"log"

	"github.com/go-co-op/gocron/v2"
	"nyihtuun.com/bentosystem/config"
	"nyihtuun.com/bentosystem/models"
)

func NotificationCleaner(ctx context.Context) {
	// create a scheduler
	scheduler, err := gocron.NewScheduler()
	if err != nil {
		log.Fatalf("unable to create scheduler: %v\n", err)
		return
	}

	// add a job to the scheduler
	job, err := scheduler.NewJob(
		gocron.CronJob(config.Cfg.CleanupCron, false),
		gocron.NewTask(func() {
			err := models.DeleteNotificationsOneWeekBefore()
			if err != nil {
				log.Printf("unable to delete notifications: %v\n", err)
				return
			}
		}),
	)
	if err != nil {
		log.Printf("unable to create job: %v\n", err)
		return
	}
	// each job has a unique id
	log.Printf("job id: %s\n", job.ID())

	// start the scheduler
	scheduler.Start()

	// block until shut down
	select {
	case <-ctx.Done():
	}

	// when done, shut it down
	err = scheduler.Shutdown()
	if err != nil {
		log.Printf("unable to shutdown scheduler: %v\n", err)
		return
	}
}
