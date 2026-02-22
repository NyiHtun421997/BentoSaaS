package worker

import (
	"context"
	"errors"
	"fmt"
	"log"
	"time"

	"github.com/IBM/sarama"
	"nyihtuun.com/bentosystem/config"
)

type notificationConsumerHandler struct{}

func (notificationConsumerHandler) Setup(sarama.ConsumerGroupSession) error   { return nil }
func (notificationConsumerHandler) Cleanup(sarama.ConsumerGroupSession) error { return nil }
func (h notificationConsumerHandler) ConsumeClaim(sess sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for msg := range claim.Messages() {
		msg.Value
		fmt.Printf("Message topic:%q partition:%d offset:%d\n", msg.Topic, msg.Partition, msg.Offset)
		sess.MarkMessage(msg, "")
	}
	return nil
}

func StartConsumer(ctx context.Context) {
	consumerConfig := sarama.NewConfig()
	consumerConfig.Consumer.Return.Errors = true
	consumerConfig.Version = sarama.V4_0_0_0
	consumerConfig.Consumer.Offsets.Initial = sarama.OffsetOldest

	consumerGroup, err := sarama.NewConsumerGroup(config.Cfg.KafkaParams.BootstrapServers,
		config.Cfg.KafkaParams.GroupID,
		consumerConfig)

	if err != nil {
		log.Fatalf("Failed to start consumer group: %v", err)
		return
	}

	defer func(consumerGroup sarama.ConsumerGroup) {
		err := consumerGroup.Close()
		if err != nil {
			log.Fatalf("Failed to close consumer group: %v", err)
		}
	}(consumerGroup)

	// Log async errors
	go func() {
		for err := range consumerGroup.Errors() {
			if err != nil {
				log.Printf("consumer group error: %v", err)
			}
		}
	}()

	topics := []string{config.Cfg.KafkaParams.TopicName}
	handler := notificationConsumerHandler{}

	for {
		if ctx.Err() != nil {
			log.Println("consumer: context canceled, exiting")
			return
		}

		err := consumerGroup.Consume(ctx, topics, handler)
		if err != nil {
			// Retry on transient errors; exit on cancellation
			if errors.Is(err, context.Canceled) || ctx.Err() != nil {
				return
			}
			log.Printf("consume error: %v (retrying)", err)
			time.Sleep(1 * time.Second)
			continue
		}
	}
}
