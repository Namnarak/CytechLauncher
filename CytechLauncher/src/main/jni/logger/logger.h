//
// Created by movte on 2025/4/25.
//

#ifndef CYTECHLAUNCHER_LOGGER_H
#define CYTECHLAUNCHER_LOGGER_H

#define LOG_E "ERROR"
#define LOG_W "WARN"
#define LOG_I "INFO"
#define LOG_D "DEBUG"

#define LOG_TO_E(...) cytech_log(LOG_E, __VA_ARGS__)
#define LOG_TO_W(...) cytech_log(LOG_W, __VA_ARGS__)
#define LOG_TO_I(...) cytech_log(LOG_I, __VA_ARGS__)
#define LOG_TO_D(...) cytech_log(LOG_D, __VA_ARGS__)

void cytech_log(const char *level, const char *fmt, ...);

#endif // CYTECHLAUNCHER_LOGGER_H