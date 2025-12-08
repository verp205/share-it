package ru.practicum.shareit.validation;

//Были проблемы с валидацией patch запросов и падали жестко тесты и поэтму нашел такое решение, не уверен, что лучшее
//Если есть более красивое решение, буду рад узнать!
public interface ValidationGroups {
    interface OnCreate {}
    interface OnUpdate {}
}