package utils

import (
	"github.com/golang-jwt/jwt/v5"
	"nyihtuun.com/bentosystem/config"
)

func VerifyToken(tokenString string) (string, error) {
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		_, ok := token.Method.(*jwt.SigningMethodHMAC)
		if !ok {
			return nil, jwt.ErrSignatureInvalid
		}
		return []byte(config.Cfg.JwtSecret), nil
	})
	if err != nil {
		return "", err
	}

	tokenIsValid := token.Valid
	if !tokenIsValid {
		return "", jwt.ErrSignatureInvalid
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		return "", jwt.ErrSignatureInvalid
	}
	userId := claims["sub"].(string)
	return userId, nil
}
