/**
 * Author:  Lukáš Kurčík <lukas.kurcik at gmail.com>
 * Created: Mar 18, 2016
 */

CREATE TABLE customer (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    phone_number VARCHAR(50) NOT NULL
 );

CREATE TABLE car (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    brand VARCHAR(50) NOT NULL,
    registration_number VARCHAR(50) NOT NULL
 );

CREATE TABLE rent (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    customer_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    price_per_day INTEGER NOT NULL,
    beginning_date DATE NOT NULL,
    expected_return_date DATE,
    real_return_date DATE
 );