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
    static class MemoryRequest {
        public int memory;
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
            ProcessData data = ctx.bodyAsClass(ProcessData.class);
            var result = l.swap(data.pid, data.memory);
            if (!result.success) {
                ctx.status(400);
                ctx.result("Swapping failed, processSize: " + data.memory);
                return;
            }
            
            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_to_end", ctx -> {
            int memory = ctx.bodyAsClass(MemoryRequest.class).memory;
            var result = l.compactToEnd(memory);
            if (!result.success) {
                ctx.status(400);
                ctx.result("Compacting to End failed, processSize: " + memory);
                return;
            }

            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_until_large_hole", ctx -> {
            int memory = ctx.bodyAsClass(MemoryRequest.class).memory;
            var result = l.compactUntilLargeHole(memory);
            if (!result.success) {
                ctx.status(400);
                ctx.result("Compacting until Large Hole failed, processSize: " + memory);
                return;
            }

            ctx.json(l.getDataForFrontend());
        });

        app.post("/api/compact_heuristically", ctx -> {
            int memory = ctx.bodyAsClass(MemoryRequest.class).memory;
            var result = l.compactHeuristically(memory);
            if (!result.success) {
                ctx.status(400);
                ctx.result("Compacting Heuristically failed, processSize: " + memory);
                return;
            }

            ctx.json(l.getDataForFrontend());
        });
    }
}