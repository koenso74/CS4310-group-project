import React, { useState, useEffect } from "react";

function App() {
  const [processList, setProcessList] = useState<number[][]>([]);
  const TOTAL_MEM = 2000;
  const UI_WIDTH = 800;


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

  // Swap a process with a new one
  const handleSwap = () => {
    fetch("http://localhost:7070/api/swap", {
      method: "POST",
    })
      .then((res) => res.json())
      .then((data) => setProcessList(data))
      .catch((err) => console.error("Error swapping:", err));
  };

  // Compacts
  const handleCompactToEnd = () => {
    fetch("http://localhost:7070/api/compact_to_end", {
      method: "POST",
    })
      .then((res) => res.json())
      .then((data) => setProcessList(data))
      .catch((err) => console.error("Error swapping:", err));
  };

  const handleCompactUntilLargeHole = () => {
    fetch("http://localhost:7070/api/compact_until_large_hole", {
      method: "POST",
    })
      .then((res) => res.json())
      .then((data) => setProcessList(data))
      .catch((err) => console.error("Error swapping:", err));
  };

  const handleCompactHeuristically = () => {
    fetch("http://localhost:7070/api/compact_heuristically", {
      method: "POST",
    })
      .then((res) => res.json())
      .then((data) => setProcessList(data))
      .catch((err) => console.error("Error swapping:", err));
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
            const end = start + size;

            return (
              <div
                key={index}
                style={{
                  position: "absolute",
                  left: `${(start / TOTAL_MEM) * UI_WIDTH}px`,
                  width: `${(size / TOTAL_MEM) * UI_WIDTH}px`,
                  height: "100px",
                  backgroundColor: "#4a90e2",
                  border: "1px solid white",
                  color: "white",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  boxSizing: "border-box",
                }}
              >
                <strong>P{pid}</strong>

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
                  {end}
                </span>
              </div>
            );
          })}
        </div>
      </div>
      <h1>Defragmentation Algorithms</h1>
      <button
        onClick={handleSwap}
        style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
      >
        Swap a process for a new process (PID: 123)
      </button>
      <div></div>
      <button
        onClick={handleCompactToEnd}
        style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
      >
        Compact To End
      </button>
      <div></div>
      <button
        onClick={handleCompactUntilLargeHole}
        style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
      >
        Compact Until Large Hole
      </button>
      <div></div>
      <button
        onClick={handleCompactHeuristically}
        style={{ padding: "10px 20px", fontSize: "16px", cursor: "pointer" }}
      >
        Compact Heuristically
      </button>
    </div>
  );
}

export default App;
