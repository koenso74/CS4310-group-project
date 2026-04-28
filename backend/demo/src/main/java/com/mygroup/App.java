package com.mygroup;

import io.javalin.Javalin;

public class App {

    public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.reflectClientOrigin = true);
            });
        }).start(7070);

        
        MemoryProcessList l = new MemoryProcessList(2000);
        app.post("api/process_list_data", ctx -> {
            ctx.json(l.getDataForFrontend());
        });
        
        app.post("/api/randomize_process_list", ctx -> {
            l.Randomize();
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/swap", ctx -> {
            l.swap(123, 300);
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_to_end", ctx -> {
            
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_until_large_hole", ctx -> {
            
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_heuristically", ctx -> {
            
            ctx.json(l.getDataForFrontend());
        });
    }
}