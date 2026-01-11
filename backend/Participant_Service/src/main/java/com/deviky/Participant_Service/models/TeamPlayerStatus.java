package com.deviky.Participant_Service.models;

public enum TeamPlayerStatus {
    ACTIVE,      // обычный член команды
    INVITED,     // приглашение капитана
    REQUESTED,   // игрок подал заявку на приватную команду
    LEAVED,      // игрок вышел сам
    KICKED,      // игрок был удалён капитаном
    CANCELED     // отклонено или отказался от заявки
}

