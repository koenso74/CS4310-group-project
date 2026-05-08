import React, { useState, useEffect, memo } from "react";

import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Bar } from "react-chartjs-2";
import ChartDataLabels from "chartjs-plugin-datalabels";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ChartDataLabels,
);

interface BatchTestResults {
  successCounts: number[];
  avgMovedProcessNums: number[];
}

function App() {
  const TOTAL_MEM = 2000;
  const UI_WIDTH = 800;
  const [processList, setProcessList] = useState<number[][]>([]);

  const [pid, setPID] = useState(0);
  const [start, setStart] = useState(0);
  const [memory, setMemory] = useState(0);

  const [pid_swap, setPIDSwap] = useState(0);
  const [memory_swap, setMemorySwap] = useState(0);

  const [memory1, setMemory1] = useState(100);
  const [memory2, setMemory2] = useState(100);
  const [memory3, setMemory3] = useState(100);

  const [batchResults, setBatchResults] = useState<BatchTestResults | null>(
    null,
  );
  const [loading, setLoading] = useState(false);

  const labels = ["Swap", "To End", "Large Hole", "Heuristic"];

  const successData = {
    labels,
    datasets: [
      {
        label: "Success Counts (out of 100)",
        data: batchResults?.successCounts || [],
        backgroundColor: "rgba(54, 162, 235, 0.6)",
        borderColor: "rgba(54, 162, 235, 1)",
        borderWidth: 1,
      },
    ],
  };

  const movedData = {
    labels,
    datasets: [
      {
        label: "Avg Processes Moved",
        data: batchResults?.avgMovedProcessNums || [],
        backgroundColor: "rgba(255, 99, 132, 0.6)",
        borderColor: "rgba(255, 99, 132, 1)",
        borderWidth: 1,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { display: true },
      datalabels: {
        display: true,
        color: "#000",
        align: "top" as const,
        anchor: "end" as const,
        offset: 4,
        font: { weight: "bold" as const },
        formatter: (value: number) =>
          value % 1 === 0 ? value : value.toFixed(2),
      },
    },
    layout: {
      padding: { top: 20 },
    },
    scales: {
      y: { beginAtZero: true },
    },
  };

  // Showing the empty process list at first
  useEffect(() => {
    fetch("http://localhost:7070/api/process_list_data")
      .then((res) => res.json())
      .then((data) => setProcessList(data))
      .catch((err) => console.error("Could not reach Java:", err));
  }, []);

  // Randomize the process list
  const handleRandomizeProcessList = () => {
    fetch("http://localhost:7070/api/randomize_process_list", {
      method: "POST",
    })
      .then((res) => res.json())
      .then((data) => setProcessList(data))
      .catch((err) => console.error("Error randomizing:", err));
  };

  // Add a process
  const handleAddProcess = () => {
    if (memory <= 0) {
      alert("Memory size must be greater than 0!");
      return;
    }

    const payload = {
      pid: Number(pid),
      start: Number(start),
      memory: Number(memory),
    };

    fetch("http://localhost:7070/api/add_process", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Server error");
        return res.json();
      })
      .then((data) => {
        console.log("New Process List:", data);

        setProcessList(data);

        setPID(0);
        setStart(0);
        setMemory(0);
      })
      .catch((err) => console.error("Error Add Process:", err));
  };
  // Swap a process with a new one
  const handleSwap = () => {
    if (pid_swap == 0) {
      alert("PID must be greater than 0!");
      return;
    }
    if (memory_swap <= 0) {
      alert("Memory size must be greater than 0!");
      return;
    }

    const payload = {
      pid: Number(pid_swap),
      start: Number(0),
      memory: Number(memory_swap),
    };

    fetch("http://localhost:7070/api/swap", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    })
      .then((res) => {
        if (!res.ok)
          return res.text().then((text) => {
            throw new Error(text);
          });
        return res.json();
      })
      .then((data) => {
        console.log("New Process List:", data);

        setProcessList(data);

        setPIDSwap(0);
        setMemorySwap(0);
      })
      .catch((err) => alert("Backend Error: " + err.message));
  };

  // Compacts
  const handleCompactToEnd = () => {
    fetch("http://localhost:7070/api/compact_to_end", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ memory: memory1 }),
    })
      .then((res) => {
        if (!res.ok)
          return res.text().then((text) => {
            throw new Error(text);
          });
        return res.json();
      })
      .then((data) => setProcessList(data))
      .catch((err) => alert("Backend Error: " + err.message));
  };

  const handleCompactUntilLargeHole = () => {
    fetch("http://localhost:7070/api/compact_until_large_hole", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ memory: memory2 }),
    })
      .then((res) => {
        if (!res.ok)
          return res.text().then((text) => {
            throw new Error(text);
          });
        return res.json();
      })
      .then((data) => setProcessList(data))
      .catch((err) => alert("Backend Error: " + err.message));
  };

  const handleCompactHeuristically = () => {
    fetch("http://localhost:7070/api/compact_heuristically", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ memory: memory3 }),
    })
      .then((res) => {
        if (!res.ok)
          return res.text().then((text) => {
            throw new Error(text);
          });
        return res.json();
      })
      .then((data) => setProcessList(data))
      .catch((err) => alert("Backend Error: " + err.message));
  };

  const runBatchTest = () => {
    setLoading(true);
    fetch("http://localhost:7070/api/batch_test", {
      method: "POST",
    })
      .then((res) => {
        if (!res.ok) throw new Error("Batch test failed on server");
        return res.json();
      })
      .then((data) => {
        setBatchResults(data);
        setLoading(false);
      })
      .catch((err) => {
        alert(err.message);
        setLoading(false);
      });
  };

  return (
    <div style={{ textAlign: "center", marginTop: "50px" }}>
      <button
        onClick={handleRandomizeProcessList}
        style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
      >
        Randomize Process List
      </button>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          gap: "20px",
          marginBottom: "20px",
        }}
      >
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
          }}
        >
          <label style={{ fontSize: "12px", fontWeight: "bold" }}>
            Process ID:
          </label>
          <input
            type="number"
            placeholder="e.g. 1"
            value={pid}
            onChange={(e) => setPID(parseInt(e.target.value, 10) || 0)}
            style={{
              padding: "5px",
              borderRadius: "4px",
              border: "1px solid #ccc",
            }}
          />
        </div>

        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
          }}
        >
          <label style={{ fontSize: "12px", fontWeight: "bold" }}>
            Start Address:
          </label>
          <input
            type="number"
            placeholder="e.g. 0"
            value={start}
            onChange={(e) => setStart(parseInt(e.target.value, 10) || 0)}
            style={{
              padding: "5px",
              borderRadius: "4px",
              border: "1px solid #ccc",
            }}
          />
        </div>

        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
          }}
        >
          <label style={{ fontSize: "12px", fontWeight: "bold" }}>
            Memory Size:
          </label>
          <input
            type="number"
            placeholder="e.g. 500"
            value={memory}
            onChange={(e) => setMemory(parseInt(e.target.value, 10) || 0)}
            style={{
              padding: "5px",
              borderRadius: "4px",
              border: "1px solid #ccc",
            }}
          />
        </div>
        <button
          onClick={handleAddProcess}
          style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
        >
          Add a Process
        </button>
      </div>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          width: "100%",
          padding: "40px 0",
        }}
      >
        <div
          style={{
            width: `${UI_WIDTH}px`,
            height: "100px",
            border: "1px solid #ccc",
            position: "relative",
            backgroundColor: "#f9f9f9",
          }}
        >
          {processList.map((proc, index) => {
            const [pid, start, size] = proc;

            return (
              <div
                key={index}
                style={{
                  position: "absolute",
                  left: `${(start / TOTAL_MEM) * UI_WIDTH}px`,
                  width: `${(size / TOTAL_MEM) * UI_WIDTH}px`,
                  height: "100px",
                  backgroundColor: pid === 0 ? "#f0f0f0" : "#4a90e2",
                  border: pid === 0 ? "1px dashed #ccc" : "1px solid white",
                  color: pid === 0 ? "#666" : "white",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  boxSizing: "border-box",
                  flexDirection: "column",
                }}
              >
                {pid === 0 ? (
                  <>
                    <span
                      style={{
                        fontSize: "12px",
                        fontWeight: "bold",
                        color: "#333",
                      }}
                    >
                      Hole
                    </span>
                    <span style={{ fontSize: "11px", color: "#333" }}>
                      {size}KB
                    </span>
                  </>
                ) : (
                  <strong>P{pid}</strong>
                )}

                {pid !== 0 && (
                  <span style={{ fontSize: "10px" }}>{size}KB</span>
                )}

                <span
                  style={{
                    position: "absolute",
                    left: "0",
                    bottom: "-20px",
                    color: "#333",
                    fontSize: "11px",
                    fontWeight: "bold",
                  }}
                >
                  {start}
                </span>
                <span
                  style={{
                    position: "absolute",
                    right: "0",
                    bottom: "-20px",
                    color: "#333",
                    fontSize: "11px",
                    fontWeight: "bold",
                  }}
                >
                  {start + size}
                </span>
              </div>
            );
          })}
        </div>
      </div>
      <h1>Defragmentation Algorithms</h1>
      <div></div>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          gap: "20px",
          marginBottom: "20px",
        }}
      >
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
          }}
        >
          <label style={{ fontSize: "12px", fontWeight: "bold" }}>
            Process ID:
          </label>
          <input
            type="number"
            placeholder="e.g. 1"
            value={pid_swap}
            onChange={(e) => setPIDSwap(parseInt(e.target.value, 10) || 0)}
            style={{
              padding: "5px",
              borderRadius: "4px",
              border: "1px solid #ccc",
            }}
          />
        </div>

        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
          }}
        >
          <label style={{ fontSize: "12px", fontWeight: "bold" }}>
            Memory Size:
          </label>
          <input
            type="number"
            placeholder="e.g. 0"
            value={memory_swap}
            onChange={(e) => setMemorySwap(parseInt(e.target.value, 10) || 0)}
            style={{
              padding: "5px",
              borderRadius: "4px",
              border: "1px solid #ccc",
            }}
          />
        </div>
        <button
          onClick={handleSwap}
          style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
        >
          Swap a process for a new process
        </button>
      </div>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          gap: "10px",
          marginBottom: "20px",
        }}
      >
        <label style={{ fontSize: "12px", fontWeight: "bold" }}>
          Memory Size:
        </label>
        <input
          type="number"
          placeholder="e.g. 500"
          value={memory1}
          onChange={(e) => setMemory1(parseInt(e.target.value, 10) || 0)}
          style={{
            padding: "5px",
            borderRadius: "4px",
            border: "1px solid #ccc",
          }}
        />
        <button
          onClick={handleCompactToEnd}
          style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
        >
          Compact To End
        </button>
      </div>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          gap: "10px",
          marginBottom: "20px",
        }}
      >
        <label style={{ fontSize: "12px", fontWeight: "bold" }}>
          Memory Size:
        </label>
        <input
          type="number"
          placeholder="e.g. 500"
          value={memory2}
          onChange={(e) => setMemory2(parseInt(e.target.value, 10) || 0)}
          style={{
            padding: "5px",
            borderRadius: "4px",
            border: "1px solid #ccc",
          }}
        />
        <button
          onClick={handleCompactUntilLargeHole}
          style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
        >
          Compact Until Large Hole
        </button>
      </div>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          gap: "10px",
          marginBottom: "20px",
        }}
      >
        <label style={{ fontSize: "12px", fontWeight: "bold" }}>
          Memory Size:
        </label>
        <input
          type="number"
          placeholder="e.g. 500"
          value={memory3}
          onChange={(e) => setMemory3(parseInt(e.target.value, 10) || 0)}
          style={{
            padding: "5px",
            borderRadius: "4px",
            border: "1px solid #ccc",
          }}
        />
        <button
          onClick={handleCompactHeuristically}
          style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
        >
          Compact Heuristically
        </button>
      </div>
      <div
        style={{ marginTop: "20px", padding: "10px", border: "1px solid #ccc" }}
      >
        <button
          onClick={runBatchTest}
          disabled={loading}
          style={{
            backgroundColor: loading ? "#ccc" : "#4CAF50",
            color: "white",
            padding: "10px 20px",
          }}
        >
          {loading ? "Running 100 Tests..." : "Run Batch Test"}
        </button>

        {batchResults && (
          <div
            style={{
              display: "flex",
              flexWrap: "wrap",
              gap: "20px",
              marginTop: "20px",
            }}
          >
            {/* Graph 1: Success Rate */}
            <div style={{ width: "45%", minWidth: "300px" }}>
              <Bar data={successData} options={chartOptions} />
            </div>

            {/* Graph 2: Efficiency */}
            <div style={{ width: "45%", minWidth: "300px" }}>
              <Bar data={movedData} options={chartOptions} />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
