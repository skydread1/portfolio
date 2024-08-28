#:post{:id "tick"
       :page :blog
       :date ["2024-04-20"]
       :title "Time as a value with Tick"
       :css-class "juxt-tick"
       :tags ["Clojure" "Java 8 time" "Tick" "Duration/Period"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                      :src-dark "/assets/loic-blog-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
Illustrate date and time concepts in programming using the Clojure Tick library: timestamp, date-time, offset, zone, instant, inst, UTC, DST, duration, period etc.
+++
## Introduction

It is always very confusing to deal with time in programming. In fact there are so many time representations, for legacy reasons, that sticking to one is not possible as our dependencies, databases or even programming languages might use different ways of representing time!

You might have asked yourself the following questions:
- Why so many time formats? `timestamp`, `date-time`, `offset-date-time`, `zoned-date-time`, `instant`, `inst`?
- What is `UTC`, `DST`?
- why use Java `Instant` instead of Java `Date`?
- Why not only deal with `timestamp`?
- How to go from one time representation to the other without getting lost?
- What is the difference between a `duration` and a `period`?

This article will answer these questions and will illustrate the answers with Clojure code snippets using the `juxt/tick` library.

## What is `Tick`?

[juxt/tick](https://github.com/juxt/tick) is an excellent open-source **Clojure** library to deal with `date` and `time` as values. The [documentation](https://juxt.github.io/tick/) is of very good quality as well.

## Time since epoch (timestamp)

The `time since epoch`, or `timestamp`, is a way of measuring time by counting the number of time units that have elapsed since a specific point in time, called the **epoch**. It is often represented in either milliseconds or seconds, depending on the level of precision required for a particular application.

So basically, it is just an `int` such as `1705752000000`

The obvious advantage is the universal simplicity of representing time. The disadvantage is the human readability. So we need to find a more human-friendly representation of time.

## Local time

*Alice is having some fish and chips for her lunch in the UK. She checks her clock on the wall and it shows 12pm. She checks her calendar and it shows the day is January the 20th.*

The local time is the time in a specific time zone, usually represented using a date and time-of-day without any time zone information. In java it is called `java.time.LocalDateTime`. However, `tick` mentioned that when you asked someone the time, it is always going to be "local", so they prefer to call it `date-time` as the local part is implicit.

So if we ask Alice for the time and date, she will reply:
```clojure
(-> (t/time "12:00")
    (t/on "2024-01-20"))
;=> #time/date-time "2024-01-20T12:00"
```

*At the same time and date Alice is having lunch in London, Bob is having some fish soup for dinner in his Singapore's nearby food court. He checked the clock on the wall and reads 8pm.*

So if we ask Bob for the time, he will reply that it is 8pm. So we can see that the local time is indeed local as Bob and Alice have different times.

The question is: how to have a common time representation for Bob and Alice?

## offset-date-time

One of the difference between Bob and Alice times is due to the Coordinated Universal Time (**UTC**). The UTC offset is the difference between the local time and the UTC time, and it is usually represented using a plus or minus sign followed by the number of hours ahead or behind UTC

The United Kingdom is located on the prime meridian, which is the reference line for measuring longitude and the basis for the UTC time standard. Therefore, the local time in the UK is always the same as UTC time, and the time zone offset is `UTC+0` (also called `Z`). Alice is on the prime meridian, therefore the time she sees is the UTC time, the universal time reference.

As you go east, the difference with UTC increase. For example, Singapore is located at approximately 103.8 degrees east longitude, which means that it is eight hours ahead of UTC, and its time zone offset is `UTC+8`. That is why Bob is 8 hours ahead of Alice (8 hours in the "future")

As you go west, the difference with UTC decrease. For example, New York City is located at approximately 74 degrees west longitude, which means that it is four hours behind UTC during standard time, and its time zone offset is `UTC-4` (4 hours behind - 4 hours in the "past").

So, going back to our example, Bob is 8 hours ahead (in the "future") of Alice as we can see via the `UTC+8`:

```clojure
;; Alice time
(-> (t/time "12:00")
    (t/on "2024-01-20")
    (t/offset-by 0))
;=> #time/offset-date-time "2024-01-20T12:00Z"

;; Bob time
(-> (t/time "12:00")
    (t/on "2024-01-20")
    (t/offset-by 8))
;=> #time/offset-date-time "2024-01-20T12:00+08:00"
```

We added the offset to our time representation, note the tick name for that representation: `offset-date-time`. In java, it is called `java.time.OffsetDateTime`. We can see for Bob's time a `+08:00`. This represents The Coordinated Universal Time (**UTC**) offset.

So we could assume that the UTC offset remains the same within the same **zone** (country or region), but it is not the case. Let's see why in the next section.

## zoned-date-time

So far we have the following components to define a time:
- date
- time
- UTC offset

However, counter-intuitively, the UTC offset for Alice is not the same all year long. Sometimes it is `UTC+0` (`Z`) in winter (as we saw earlier) but sometimes it is `UTC+1` in summer.

Let me prove it to you:
```clojure
;; time for Alice in winter
(-> (t/time "12:00")
    (t/on "2024-01-20") ;; January - a winter month
    (t/in "Europe/London")
    (t/offset-date-time))
;=> #time/offset-date-time "2024-01-20T12:00Z"

;; time for Alice in summer
(-> (t/time "12:00")
    (t/on "2024-08-20") ;; August - a summer month
    (t/in "Europe/London")
    (t/offset-date-time))
;=> #time/offset-date-time "2024-08-20T12:00+01:00"
```

This UTC offset difference is due to the Daylight Saving Time (**DST**).

Daylight Saving Time (DST) is a system of adjusting the clock in order to make better use of daylight during the summer months by setting the clock forward by one hour in the spring and setting it back by one hour in the fall. This way, Alice can enjoy more of the sunlight in summer since the days are "longer" (more sunlight duration) while keeping her same working hours!

It is important to note that not all countries implement DSL. Some countries do not use DSL because they don't need. That is the case of Singapore. In Singapore, the sunset/sunrise is almost happening at the same time everyday so technically, there is no Winter/Summer. Some country chose not to use it. That's the case of Japan for instance. Japan could benefit from the DSL but chose not to implement it for diverse reasons.

So we can conclude that a UTC offset is not representative of a Zone because some country might implement DST and other not. Also, for the country implementing DST, their UTC is therefore not fix throughout the year. Thus, we need another parameter to fully define a time: the **Zone**:

```clojure
(-> (t/time "12:00")
    (t/on "2024-01-20") ;; January - a winter month
    (t/in "Europe/London"))
;=> #time/zoned-date-time "2024-01-20T12:00Z[Europe/London]"
```

You can notice that it is the same code as before but I remove the conversion to an `offset-date-time`. Indeed, Adding the zone like in `(t/in "Europe/London")` is already considering the **Zone** obviously (and therefore the  **UTC**) thus creating a `zoned-date-time`.

A `#time/zoned-date-time` in Java is called a `java.time.ZonedDateTime`.

So we now have a complete way to describe the time:
- a date
- a time
- a zone (that includes the location and the UTC encapsulating the DST)

So the time for Bob is:
```clojure
(-> (t/time "12:00")
    (t/on "2024-01-20")
    (t/in "Asia/Singapore"))
;=> #time/zoned-date-time "2024-01-20T12:00+08:00[Asia/Singapore]"
```

So to recap:
- the **Zone** `Asia/Singapore` always has the same **UTC** all year long because no **DST**
- the **Zone** `Europe/London` has a different **UTC** in summer and winter
- thus Bob is ahead of Alice by 8 hours during winter and Bob is ahead of Alice by 7 hours during summer.
- This is due by the fact that the UK implements **DST** which makes its own **UTC** throughout the year.

So a **Zone** encapsulates the notion of **UTC** and **DST**.

## instant

You might thought we were done here but actually the recommended time representation would be an `instant`. In java, it is called `java.time.Instant`. Why do we want to use instant is actually to avoid confusion. When you store a time in your DB, or when you want to add 10 days to this time, you actually don't want to deal with time zone. In programming, we always want to have a solution as simple as possible. Remember the very first time representation I mentioned? The **time since epoch**. The `epoch` in the prime meridian (`UTC+0`) is the same for everybody. So the time since epoch (to current UTC+0 time) in ms is a universal way of representing the time.

```clojure
;; instant time for Alice
(-> (t/time "12:00")
    (t/on "2024-01-20")
    (t/in "Europe/London")
    (t/instant))
;=> #time/instant "2024-01-20T12:00:00Z"

;; instant time for Bob
(-> (t/time "20:00")
    (t/on "2024-01-20")
    (t/in "Asia/Singapore")
    (t/instant))
;=> #time/instant "2024-01-20T12:00:00Z"
```

We can see in the example above, that since Singapore is 8 hours ahead of London, 12pm in London and 8pm in Singapore are indeed the same `instant`.

The `instant` is the human-friendly time representation of the timestamp (time since epoch). You can then store that format in your DB or do operation on it such as adding/substituting duration or period to it (more on this later).

The `epoch` in time-since-epoch is equivalent to #time/instant "1970-01-01T00:00:00Z":
```clojure
(t/epoch)
;=> #time/instant "1970-01-01T00:00:00Z"
```

## Alice and Bob don't care about instants

That is correct, if we have a web page, we want Alice to see the time in London time and Bob the time in Singapore time. This is easy to do. we can derive the `zoned-date-time` from an `instant` since we know the zone of Bob and Alice:

```clojure
;; in Alice's browser
(t/format (t/formatter "yyyy-MM-dd HH:mm:ss")
          (t/in #time/instant "2024-01-20T12:00:00Z" "Europe/London"))
"2024-01-20 12:00:00"

;; in Bob's browser
(t/format (t/formatter "yyyy-MM-dd HH:mm:ss")
          (t/in #time/instant "2024-01-20T12:00:00Z" "Asia/Singapore"))
"2024-01-20 20:00:00"
```

## inst

Last time format I promise. As a clojure developer, you might often see `inst`. It is **different** from `instant`. In java `inst` is called `java.util.Date`. The `java.util.Date` class is an old and flawed class that was replaced by the Java 8 time API, and it should be avoided when possible.

However, some libraries might require you to pass `inst` instead of `instant` still, and it is easy to convert between the two using the Tick library:

```clojure
(t/inst #time/instant "2024-01-20T04:00:00Z")
;=> #inst "2024-01-20T04:00:00.000-00:00"
```

What about the other way around?

```clojure
(t/instant #inst "2024-01-20T04:00:00.000-00:00")
;=> #time/instant "2024-01-20T04:00:00Z"
```

## All theses time formats are confusing

Just remember these key points:
- to store or do operations on time, use `instant` (java.time.Instant)
- to represent time locally for users, convert your instant to `zoned-date-time` (java.time.ZonedDateTime)
- to have a human readable format aka browser, parse your `zoned-date-time` using string formatter
- if a third party lib needs other format, use tick intuitive conversion functions (t/inst, t/instant etc)

## Duration vs Period

We now know that we need to use `instant` to perform operations on time. However, sometimes we use `duration` and sometimes we use `period`:

```clojure
(t/new-duration 10 :seconds)
;=> #time/duration "PT10S"

(t/new-period 10 :weeks)
;=> #time/period "P70D"
```

They are not interchangeable:
```clojure
(t/new-period 10 :seconds)
; Execution error (IllegalArgumentException) at tick.core/new-period (core.cljc:649).
; No matching clause: :seconds
```

So what is the difference? I will give you a clue:
- all units from `nanosecond` to `day` (included) are `durations`
- all units from `day` such as a `week` for instance are a `period`.

There is one unit that can be both a `duration` and a `period`: a `day`:

```clojure
;; day as duration
(t/new-duration 10 :days)
#time/duration "PT240H"

;; day as period
(t/new-period 10 :days)
#time/period "P10D"
```

Therefore, a simple definition could be:
- a `duration` measures an amount of time using time-based values (seconds, nanoseconds). 
- a `period` uses date-based (we can also calendar-based) values (years, months, days)
- a `day` can be both `duration` and `period`: a duration of one day is exactly 24 hours long but a period of one day, when considering the calendar, may vary.

First, here is how you would add a day as duration or as a period to the proper format:

```clojure
;; time-based so use duration
(-> (t/time "10:00")
    (t/>> (t/new-duration 4 :hours)))
;=> #time/time "14:00"

;; date-based so use period
(-> (t/date "2024-04-01")
    (t/>> (t/new-period 1 :days)))
;=> #time/date "2024-04-02"
```

Now, let me prove to you that we need to be careful to chose the right format for a day. In London, at 1am on the last Sunday of March, the clocks go forward 1 hour (DST increase by one because we enter summer months). So in 2024, at 1am, on March 31st, clocks go forward 1 hour.

```clojure
;; we add a period of 1 day
(-> (t/time "08:00")
    (t/on "2024-03-30")
    (t/in "Europe/London")
    (t/>> (t/new-period 1 :days)))
#time/zoned-date-time "2024-03-31T08:00+01:00[Europe/London]"

;; we add a duration of 1 day
(-> (t/time "08:00")
    (t/on "2024-03-30")
    (t/in "Europe/London")
    (t/>> (t/new-duration 1 :days)))
#time/zoned-date-time "2024-03-31T09:00+01:00[Europe/London]"
```

We can see that since in this specific DST update to summer month, the day 03/31 "gained" an hour so it has a `duration` of 25 hours, therefore our new time is `09:00`. However, the `period` taking into consideration the date in a calendar system, does not see a day as 24 hours (time-base) but as calendar unit (date-based) and therefore the new time is still `08:00`.

## Conclusion

A **Zone** encapsulates the notion of **UTC** and **DST**.

The **time since epoch** is the universal *computer-friendly* of representing time whereas the **Instant** is the universal *human-friendly* of representing time.

A `duration` measures an amount of time using time-based values whereas a `period` uses date-based (calendar) values.

Finally, for Clojure developers, I highly recommend using `juxt/tick` as it allows us to handle time efficiently (conversion, operations) and elegantly (readable, as values) and I use it in several of my projects. It is also of course possible to do interop with the `java.time.Instant` class directly if you prefer.
