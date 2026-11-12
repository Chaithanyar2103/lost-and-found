package com.example.lostfound;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

    private final List<Item> items = new CopyOnWriteArrayList<>();
    private final AtomicLong ids = new AtomicLong();

    @GetMapping("/items")
    public List<Item> list(@RequestParam(required = false) String status) {
        if (status == null || status.isBlank()) {
            return items;
        }
        return items.stream().filter(i -> i.getStatus().equalsIgnoreCase(status.trim())).toList();
    }

    @PostMapping("/items")
    public ResponseEntity<Item> create(@RequestBody ItemRequest req) {
        if (isBlank(req.name()) || isBlank(req.location())) {
            return ResponseEntity.badRequest().build();
        }
        String status = isBlank(req.status()) ? "LOST" : req.status().trim().toUpperCase();
        if (!status.equals("LOST") && !status.equals("FOUND")) {
            return ResponseEntity.badRequest().build();
        }
        Item item = new Item(ids.incrementAndGet(), req.name().trim(),
                req.description() == null ? "" : req.description().trim(),
                req.location().trim(), status,
                req.contact() == null ? "" : req.contact().trim());
        items.add(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PostMapping("/items/{id}/claim")
    public ResponseEntity<Item> claim(@PathVariable long id) {
        Optional<Item> found = items.stream().filter(i -> i.getId() == id).findFirst();
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        found.get().claim();
        return ResponseEntity.ok(found.get());
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        boolean removed = items.removeIf(i -> i.getId() == id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
