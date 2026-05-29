cmake_minimum_required(VERSION 3.22.1)

project("particleflow")

add_library(
        particleflow
        SHARED
        particleflow.cpp)

target_link_libraries(particleflow
        android
        log)
