package config

import (
	"log"
	"strings"

	"github.com/spf13/viper"
)

type Config struct {
	DBParams struct {
		User     string `mapstructure:"user"`
		Password string `mapstructure:"password"`
		Address  string `mapstructure:"address"`
		Port     int    `mapstructure:"port"`
		DbName   string `mapstructure:"dbname"`
	} `mapstructure:"db-params"`

	KafkaParams struct {
		BootstrapServers []string `mapstructure:"bootstrap-servers"`
		TopicName        string   `mapstructure:"topic-name"`
		GroupID          string   `mapstructure:"group-id"`
	} `mapstructure:"kafka-params"`
	ServerAddress string `mapstructure:"server-address"`
	ServerPort    int    `mapstructure:"server-port"`
	JwtSecret     string `mapstructure:"jwt-secret"`
	CleanupCron   string `mapstructure:"cleanup-cron"`
}

var Cfg Config

func LoadConfig() {
	viper.AddConfigPath("./config")
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")

	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_", "-", "_"))
	viper.SetEnvPrefix("NOTI")
	viper.AutomaticEnv()

	err := viper.ReadInConfig()
	if err != nil {
		log.Fatalf("Error reading config file, %s", err)
		return
	}

	err = viper.Unmarshal(&Cfg)
	if err != nil {
		log.Fatalf("Unable to decode into struct, %v", err)
		return
	}
}
