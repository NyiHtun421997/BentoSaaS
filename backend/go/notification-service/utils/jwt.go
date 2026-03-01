package utils

import (
	"encoding/base64"
	"log"
	"strings"

	"github.com/golang-jwt/jwt/v5"
	"nyihtuun.com/bentosystem/config"
)

func VerifyToken(tokenString string) (string, error) {
	secret := strings.TrimSpace(config.Cfg.JwtSecretKey)
	if secret == "" {
		return "", jwt.ErrSignatureInvalid
	}

	claims := jwt.MapClaims{}

	token, err := jwt.ParseWithClaims(
		tokenString,
		claims,
		func(token *jwt.Token) (interface{}, error) {
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, jwt.ErrSignatureInvalid
			}

			decoded, err := base64.StdEncoding.DecodeString(secret)
			if err != nil {
				log.Println("Failed to base64 decode secret:", err)
				return nil, err
			}

			return decoded, nil
		},
		jwt.WithValidMethods([]string{"HS256", "HS384", "HS512"}),
	)
	if err != nil {
		log.Println("Error parsing token:", err)
		return "", err
	}
	if token == nil || !token.Valid {
		log.Println("Token is invalid")
		return "", jwt.ErrSignatureInvalid
	}

	log.Println("Token is valid and trying to extract user id")
	sub, ok := claims["sub"]
	if !ok {
		return "", jwt.ErrSignatureInvalid
	}
	userId, ok := sub.(string)
	if !ok || strings.TrimSpace(userId) == "" {
		return "", jwt.ErrSignatureInvalid
	}
	return userId, nil
}
