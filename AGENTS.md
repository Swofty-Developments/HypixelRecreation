# Agent Instructions

## General Principles

* Prefer the smallest correct change that fully solves the requested task.
* Follow existing project patterns before introducing new patterns.
* Do not introduce abstractions for hypothetical future use.
* Do not add new interfaces, base classes, managers, services, utilities, or wrappers when a straightforward
  implementation is sufficient.
* Reuse existing utilities and infrastructure where appropriate instead of creating parallel implementations.

## Hypixel Behavior

* This project aims to reproduce observable Hypixel behavior as closely as possible.
* When implementing a mechanic, look for analogous implementations elsewhere in the repository before designing a new
  system.
* Do not invent additional mechanics, restrictions, messages, cooldowns, side effects, or balancing changes that were
  not requested or established by existing behavior.
* Keep player-facing behavior, timing, messages, item behavior, and game rules consistent with Hypixel's implementation.

## Minestom

* Prefer existing project wrappers and utilities around Minestom APIs when they already solve the problem.
* Be conscious of server-thread performance. Do not introduce blocking I/O or expensive work into frequently executed
  events, ticks, packet handlers, or scheduled tasks.
* Do not introduce custom thread scheduling when existing project or Minestom scheduling infrastructure is appropriate.
* Preserve existing threading and synchronization assumptions when modifying asynchronous or shared state.

## Code Style

* Prefer clear names and straightforward control flow over explanatory comments.
* Do not add comments that restate what the code does.
* Add comments only for genuinely non-obvious algorithms, invariants, protocol behavior, or business logic.
* Do not add unnecessary Javadocs.
* Use Lombok
* Use Java 25 features when they improve the implementation; do not rewrite code solely to use newer language features.
* Avoid unnecessary abstraction and premature generalization. However, when recreating Hypixel behavior, this may be
  warranted if not encouraged.
* Avoid duplicating constants or utilities that already exist in the project.
* Preserve public APIs unless changing them is part of the task.
