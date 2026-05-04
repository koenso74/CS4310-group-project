package com.mygroup;

import io.javalin.Javalin;

public class App {

    static class ProcessData {
        public int pid;
        public int start;
        public int memory;

        public ProcessData() {}

        public ProcessData(int pid, int start, int memory) {
            this.pid = pid;
            this.start = start;
            this.memory = memory;
        }
    }

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

        app.post("/api/add_process", ctx -> {
            ProcessData data = ctx.bodyAsClass(ProcessData.class);
            l.addProcessAt(data.pid, data.memory, data.start);
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/swap", ctx -> {
            l.swap(123, 300);
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_to_end", ctx -> {
            l.compactToEnd();
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_until_large_hole", ctx -> {
            l.compactUntilLargeHole(300);
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_heuristically", ctx -> {
            l.compactHeuristically(300);
            ctx.json(l.getDataForFrontend());
        });
    }
}