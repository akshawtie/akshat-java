<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.nio.file.*, java.nio.charset.StandardCharsets, java.util.*, java.util.regex.*, java.time.LocalDate" %>
<%!
    // -------------------------------------------------------------
    // Data Model: CalorieLogItem
    // -------------------------------------------------------------
    public static class CalorieLogItem {
        private String date;
        private String name;
        private double carbs;
        private double protein;
        private double fats;
        private double totalCalories;

        public CalorieLogItem(String date, String name, double carbs, double protein, double fats, double totalCalories) {
            this.date = (date != null && !date.trim().isEmpty()) ? date.trim() : LocalDate.now().toString();
            this.name = (name != null) ? name.trim() : "Unnamed";
            this.carbs = Math.max(0, carbs);
            this.protein = Math.max(0, protein);
            this.fats = Math.max(0, fats);
            if (totalCalories <= 0) {
                this.totalCalories = (this.carbs * 4.0) + (this.protein * 4.0) + (this.fats * 9.0);
            } else {
                this.totalCalories = totalCalories;
            }
        }

        public String getDate() { return date; }
        public String getName() { return name; }
        public double getCarbs() { return carbs; }
        public double getProtein() { return protein; }
        public double getFats() { return fats; }
        public double getTotalCalories() { return totalCalories; }

        public double getCarbCalories() { return carbs * 4.0; }
        public double getProteinCalories() { return protein * 4.0; }
        public double getFatCalories() { return fats * 9.0; }

        public int getCarbPercent() {
            return totalCalories > 0 ? (int) Math.round((getCarbCalories() / totalCalories) * 100) : 0;
        }

        public int getProteinPercent() {
            return totalCalories > 0 ? (int) Math.round((getProteinCalories() / totalCalories) * 100) : 0;
        }

        public int getFatPercent() {
            return totalCalories > 0 ? Math.max(0, 100 - getCarbPercent() - getProteinPercent()) : 0;
        }
    }

    // -------------------------------------------------------------
    // Helper: Resolve calorie-log.json file path
    // -------------------------------------------------------------
    private File resolveLogFile(ServletContext context) {
        // Candidate 1: User's workspace location
        File candidate1 = new File("/run/media/awkwart/New Volume/JAVA/calorie-log.json");
        if (candidate1.exists() || (candidate1.getParentFile() != null && candidate1.getParentFile().canWrite())) {
            return candidate1;
        }

        // Candidate 2: Current working directory
        File candidate2 = new File("calorie-log.json");
        if (candidate2.exists()) {
            return candidate2;
        }

        // Candidate 3: ServletContext webapp root
        if (context != null) {
            String webappPath = context.getRealPath("/");
            if (webappPath != null) {
                File candidate3 = new File(webappPath, "calorie-log.json");
                if (candidate3.exists()) return candidate3;

                File candidate4 = new File(webappPath, "WEB-INF/calorie-log.json");
                if (candidate4.exists()) return candidate4;

                return candidate3; // fallback to create in webapp directory
            }
        }

        return candidate1;
    }

    // -------------------------------------------------------------
    // Helper: Load log entries from JSON (Pure Java, zero dependencies)
    // -------------------------------------------------------------
    private List<CalorieLogItem> loadLogs(File file) {
        List<CalorieLogItem> list = new ArrayList<>();
        if (file == null || !file.exists()) {
            return list;
        }

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            // Matches any JSON object inside the array
            Pattern objectPattern = Pattern.compile("\\{([^}]+)\\}", Pattern.DOTALL);
            Matcher matcher = objectPattern.matcher(content);

            while (matcher.find()) {
                String objBody = matcher.group(1);

                String date = extractStringField(objBody, "date");
                String name = extractStringField(objBody, "name");
                double carbs = extractDoubleField(objBody, "carbs");
                double protein = extractDoubleField(objBody, "protein");
                double fats = extractDoubleField(objBody, "fats");
                double totalCalories = extractDoubleField(objBody, "totalCalories");

                if (date == null || date.isEmpty()) date = LocalDate.now().toString();
                if (name == null || name.isEmpty()) name = "Unknown Item";

                list.add(new CalorieLogItem(date, name, carbs, protein, fats, totalCalories));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // -------------------------------------------------------------
    // Helper: Save log entries to JSON file
    // -------------------------------------------------------------
    private synchronized boolean saveLogs(File file, List<CalorieLogItem> logs) {
        if (file == null) return false;
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[\n");
            for (int i = 0; i < logs.size(); i++) {
                CalorieLogItem item = logs.get(i);
                sb.append("  {\n");
                sb.append("    \"date\": \"").append(escapeJson(item.getDate())).append("\",\n");
                sb.append("    \"name\": \"").append(escapeJson(item.getName())).append("\",\n");
                sb.append(String.format(Locale.US, "    \"carbs\": %.1f,\n", item.getCarbs()));
                sb.append(String.format(Locale.US, "    \"protein\": %.1f,\n", item.getProtein()));
                sb.append(String.format(Locale.US, "    \"fats\": %.1f,\n", item.getFats()));
                sb.append(String.format(Locale.US, "    \"totalCalories\": %.1f\n", item.getTotalCalories()));
                sb.append("  }");
                if (i < logs.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("]\n");

            Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String extractStringField(String block, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(block);
        return m.find() ? m.group(1) : "";
    }

    private double extractDoubleField(String block, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9.]+)");
        Matcher m = p.matcher(block);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    // -------------------------------------------------------------
    // Request Handling (Add / Delete / Search)
    // -------------------------------------------------------------
    File logFile = resolveLogFile(application);
    List<CalorieLogItem> logs = loadLogs(logFile);

    String alertMessage = null;
    String alertType = "info"; // "success", "danger", "info"

    String method = request.getMethod();
    String action = request.getParameter("action");

    if ("POST".equalsIgnoreCase(method)) {
        if ("add".equalsIgnoreCase(action)) {
            String name = request.getParameter("name");
            String carbsStr = request.getParameter("carbs");
            String proteinStr = request.getParameter("protein");
            String fatsStr = request.getParameter("fats");
            String date = request.getParameter("date");

            if (name == null || name.trim().isEmpty()) {
                alertMessage = "Please enter a valid meal or user name.";
                alertType = "danger";
            } else {
                try {
                    double carbs = Double.parseDouble(carbsStr.trim());
                    double protein = Double.parseDouble(proteinStr.trim());
                    double fats = Double.parseDouble(fatsStr.trim());

                    if (carbs < 0 || protein < 0 || fats < 0) {
                        alertMessage = "Macros cannot be negative values.";
                        alertType = "danger";
                    } else {
                        double total = (carbs * 4.0) + (protein * 4.0) + (fats * 9.0);
                        if (date == null || date.trim().isEmpty()) {
                            date = LocalDate.now().toString();
                        }

                        CalorieLogItem newItem = new CalorieLogItem(date, name, carbs, protein, fats, total);
                        logs.add(newItem);

                        boolean saved = saveLogs(logFile, logs);
                        if (saved) {
                            alertMessage = String.format(Locale.US, "Success! Logged '<strong>%s</strong>' with <strong>%.1f kcal</strong>.", escapeHtml(name), total);
                            alertType = "success";
                        } else {
                            alertMessage = "Entry added to session, but failed to write to " + logFile.getName() + " (Permission error).";
                            alertType = "warning";
                        }
                    }
                } catch (NumberFormatException ex) {
                    alertMessage = "Please enter valid numeric values for Carbs, Protein, and Fats.";
                    alertType = "danger";
                }
            }
        } else if ("delete".equalsIgnoreCase(action)) {
            String indexStr = request.getParameter("index");
            try {
                int idx = Integer.parseInt(indexStr);
                if (idx >= 0 && idx < logs.size()) {
                    CalorieLogItem removed = logs.remove(idx);
                    saveLogs(logFile, logs);
                    alertMessage = "Removed entry '<strong>" + escapeHtml(removed.getName()) + "</strong>'.";
                    alertType = "success";
                }
            } catch (Exception ex) {
                alertMessage = "Could not delete entry: invalid index.";
                alertType = "danger";
            }
        }
    }

    // -------------------------------------------------------------
    // Filters & Analytics Calculation
    // -------------------------------------------------------------
    String searchQuery = request.getParameter("search");
    if (searchQuery != null) searchQuery = searchQuery.trim();
    else searchQuery = "";

    String todayDate = LocalDate.now().toString();
    double totalCaloriesToday = 0;
    double totalCaloriesAll = 0;
    double totalCarbsAll = 0;
    double totalProteinAll = 0;
    double totalFatsAll = 0;
    int todayCount = 0;

    for (CalorieLogItem item : logs) {
        totalCaloriesAll += item.getTotalCalories();
        totalCarbsAll += item.getCarbs();
        totalProteinAll += item.getProtein();
        totalFatsAll += item.getFats();
        if (todayDate.equals(item.getDate())) {
            totalCaloriesToday += item.getTotalCalories();
            todayCount++;
        }
    }

    double avgCalories = logs.isEmpty() ? 0 : (totalCaloriesAll / logs.size());
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Calorie & Macro Tracker</title>
    <!-- Modern Typography -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;600;700&display=swap" rel="stylesheet">

    <style>
        :root {
            --bg-color: #0b0f17;
            --surface-color: #121826;
            --surface-card: #182234;
            --surface-card-hover: #1f2c42;
            --border-color: rgba(255, 255, 255, 0.08);
            --border-highlight: rgba(99, 102, 241, 0.35);

            --text-primary: #f8fafc;
            --text-secondary: #94a3b8;
            --text-muted: #64748b;

            --primary: #6366f1;
            --primary-hover: #4f46e5;
            --primary-glow: rgba(99, 102, 241, 0.25);

            --accent-carb: #38bdf8;
            --accent-protein: #34d399;
            --accent-fat: #fb923c;
            --accent-cal: #f43f5e;

            --success-bg: rgba(16, 185, 129, 0.15);
            --success-border: #10b981;
            --danger-bg: rgba(239, 68, 68, 0.15);
            --danger-border: #ef4444;
            --warning-bg: rgba(245, 158, 11, 0.15);
            --warning-border: #f59e0b;

            --radius-sm: 8px;
            --radius-md: 12px;
            --radius-lg: 18px;
            --radius-full: 9999px;
            --transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
            background-color: var(--bg-color);
            color: var(--text-primary);
            line-height: 1.5;
            min-height: 100vh;
            padding: 24px 16px;
            background-image: 
                radial-gradient(circle at 15% 10%, rgba(99, 102, 241, 0.12) 0%, transparent 40%),
                radial-gradient(circle at 85% 80%, rgba(244, 63, 94, 0.08) 0%, transparent 40%);
            background-attachment: fixed;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        /* Top Header */
        header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 0 28px;
            border-bottom: 1px solid var(--border-color);
            margin-bottom: 28px;
            flex-wrap: wrap;
            gap: 16px;
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .brand-icon {
            width: 44px;
            height: 44px;
            background: linear-gradient(135deg, #f43f5e, #f97316);
            border-radius: var(--radius-md);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            box-shadow: 0 4px 14px rgba(244, 63, 94, 0.35);
        }

        .brand-text h1 {
            font-size: 22px;
            font-weight: 800;
            letter-spacing: -0.5px;
            background: linear-gradient(to right, #ffffff, #cbd5e1);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .brand-text p {
            font-size: 13px;
            color: var(--text-secondary);
        }

        .header-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 13px;
            color: var(--text-secondary);
        }

        .badge-pill {
            background: var(--surface-card);
            border: 1px solid var(--border-color);
            padding: 6px 14px;
            border-radius: var(--radius-full);
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        .badge-pill .dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #10b981;
            box-shadow: 0 0 8px #10b981;
        }

        /* Analytics Overview Cards */
        .analytics-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 18px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: var(--surface-color);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-lg);
            padding: 20px;
            position: relative;
            overflow: hidden;
            transition: var(--transition);
        }

        .stat-card:hover {
            transform: translateY(-2px);
            border-color: var(--border-highlight);
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
        }

        .stat-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 3px;
        }

        .stat-card.cal::before { background: linear-gradient(90deg, #f43f5e, #fb7185); }
        .stat-card.carbs::before { background: linear-gradient(90deg, #38bdf8, #0284c7); }
        .stat-card.protein::before { background: linear-gradient(90deg, #34d399, #059669); }
        .stat-card.fats::before { background: linear-gradient(90deg, #fb923c, #ea580c); }

        .stat-title {
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.6px;
            font-weight: 700;
            color: var(--text-muted);
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .stat-value {
            font-size: 28px;
            font-weight: 800;
            font-family: 'JetBrains Mono', monospace;
            color: var(--text-primary);
            line-height: 1.1;
        }

        .stat-subtext {
            font-size: 12px;
            color: var(--text-secondary);
            margin-top: 8px;
        }

        /* Notification Alert */
        .alert {
            padding: 14px 18px;
            border-radius: var(--radius-md);
            margin-bottom: 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 14px;
            border-left: 4px solid;
            animation: fadeIn 0.3s ease-in-out;
        }

        .alert.success {
            background: var(--success-bg);
            border-color: var(--success-border);
            color: #6ee7b7;
        }

        .alert.danger {
            background: var(--danger-bg);
            border-color: var(--danger-border);
            color: #fca5a5;
        }

        .alert.warning {
            background: var(--warning-bg);
            border-color: var(--warning-border);
            color: #fde68a;
        }

        .alert-close {
            background: transparent;
            border: none;
            color: inherit;
            cursor: pointer;
            font-size: 18px;
            opacity: 0.7;
            transition: var(--transition);
        }
        .alert-close:hover { opacity: 1; }

        /* Main Workspace Split */
        .main-layout {
            display: grid;
            grid-template-columns: 380px 1fr;
            gap: 24px;
            align-items: start;
        }

        @media (max-width: 960px) {
            .main-layout {
                grid-template-columns: 1fr;
            }
        }

        /* Panel Container */
        .panel {
            background: var(--surface-color);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-lg);
            padding: 24px;
            box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
        }

        .panel-header {
            margin-bottom: 20px;
        }

        .panel-title {
            font-size: 18px;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .panel-subtitle {
            font-size: 13px;
            color: var(--text-secondary);
            margin-top: 4px;
        }

        /* Form Inputs */
        .form-group {
            margin-bottom: 16px;
        }

        label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            color: var(--text-secondary);
            margin-bottom: 6px;
        }

        .input-row {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr;
            gap: 10px;
        }

        input[type="text"],
        input[type="number"],
        input[type="date"] {
            width: 100%;
            background: var(--surface-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-sm);
            padding: 10px 12px;
            font-size: 14px;
            color: var(--text-primary);
            font-family: inherit;
            outline: none;
            transition: var(--transition);
        }

        input[type="number"] {
            font-family: 'JetBrains Mono', monospace;
        }

        input:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px var(--primary-glow);
            background: #1c273c;
        }

        /* Quick Preset Chips */
        .presets-label {
            font-size: 12px;
            color: var(--text-muted);
            margin-bottom: 8px;
            text-transform: uppercase;
            font-weight: 600;
            letter-spacing: 0.5px;
        }

        .preset-chips {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-bottom: 18px;
        }

        .preset-btn {
            background: var(--surface-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-full);
            color: var(--text-secondary);
            font-size: 12px;
            padding: 5px 11px;
            cursor: pointer;
            transition: var(--transition);
            font-family: inherit;
        }

        .preset-btn:hover {
            background: var(--surface-card-hover);
            color: var(--text-primary);
            border-color: var(--border-highlight);
        }

        /* Live Preview Calculator */
        .live-preview-box {
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.08), rgba(244, 63, 94, 0.08));
            border: 1px dashed rgba(99, 102, 241, 0.4);
            border-radius: var(--radius-md);
            padding: 16px;
            margin-bottom: 20px;
        }

        .live-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }

        .live-header span {
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: var(--text-secondary);
        }

        .live-cals {
            font-size: 22px;
            font-weight: 800;
            font-family: 'JetBrains Mono', monospace;
            color: #fb7185;
        }

        /* Macro Bar */
        .macro-ratio-bar {
            height: 10px;
            width: 100%;
            background: rgba(255, 255, 255, 0.06);
            border-radius: var(--radius-full);
            display: flex;
            overflow: hidden;
            margin: 10px 0 8px;
        }

        .bar-carb { background: var(--accent-carb); height: 100%; transition: width 0.3s; }
        .bar-protein { background: var(--accent-protein); height: 100%; transition: width 0.3s; }
        .bar-fat { background: var(--accent-fat); height: 100%; transition: width 0.3s; }

        .macro-legend {
            display: flex;
            justify-content: space-between;
            font-size: 11px;
            font-family: 'JetBrains Mono', monospace;
            color: var(--text-secondary);
        }

        .legend-item {
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .legend-dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
        }

        .dot-carb { background: var(--accent-carb); }
        .dot-protein { background: var(--accent-protein); }
        .dot-fat { background: var(--accent-fat); }

        /* Submit Button */
        .btn-submit {
            width: 100%;
            background: linear-gradient(135deg, var(--primary), var(--primary-hover));
            border: none;
            border-radius: var(--radius-md);
            padding: 12px 18px;
            color: #ffffff;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            transition: var(--transition);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            box-shadow: 0 4px 14px var(--primary-glow);
        }

        .btn-submit:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
            filter: brightness(1.08);
        }

        .btn-submit:active {
            transform: translateY(0);
        }

        /* History Table Section */
        .table-controls {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 16px;
            flex-wrap: wrap;
            gap: 12px;
        }

        .search-box {
            position: relative;
            max-width: 260px;
            width: 100%;
        }

        .search-box input {
            padding-left: 32px;
            border-radius: var(--radius-full);
            font-size: 13px;
        }

        .search-icon {
            position: absolute;
            left: 11px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-muted);
            font-size: 14px;
            pointer-events: none;
        }

        .table-wrapper {
            overflow-x: auto;
            border-radius: var(--radius-md);
            border: 1px solid var(--border-color);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13.5px;
            text-align: left;
        }

        thead {
            background: var(--surface-card);
            border-bottom: 1px solid var(--border-color);
        }

        th {
            padding: 12px 16px;
            font-weight: 700;
            color: var(--text-secondary);
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        tbody tr {
            border-bottom: 1px solid var(--border-color);
            transition: var(--transition);
        }

        tbody tr:last-child {
            border-bottom: none;
        }

        tbody tr:hover {
            background: rgba(255, 255, 255, 0.02);
        }

        td {
            padding: 12px 16px;
            vertical-align: middle;
        }

        .cell-name {
            font-weight: 600;
            color: var(--text-primary);
        }

        .cell-date {
            font-family: 'JetBrains Mono', monospace;
            font-size: 12px;
            color: var(--text-muted);
            white-space: nowrap;
        }

        .cell-num {
            font-family: 'JetBrains Mono', monospace;
            font-weight: 600;
            white-space: nowrap;
        }

        .badge-carb { color: var(--accent-carb); }
        .badge-protein { color: var(--accent-protein); }
        .badge-fat { color: var(--accent-fat); }
        
        .badge-cal {
            background: rgba(244, 63, 94, 0.14);
            color: #fb7185;
            padding: 4px 8px;
            border-radius: var(--radius-sm);
            font-weight: 700;
            font-family: 'JetBrains Mono', monospace;
            display: inline-block;
        }

        .mini-ratio {
            width: 70px;
            height: 6px;
            background: rgba(255, 255, 255, 0.08);
            border-radius: var(--radius-full);
            display: flex;
            overflow: hidden;
            margin-top: 4px;
        }

        .btn-delete {
            background: transparent;
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #f87171;
            padding: 4px 10px;
            border-radius: var(--radius-sm);
            cursor: pointer;
            font-size: 12px;
            transition: var(--transition);
        }

        .btn-delete:hover {
            background: var(--danger-bg);
            border-color: var(--danger-border);
            color: #ffffff;
        }

        .empty-state {
            padding: 40px 20px;
            text-align: center;
            color: var(--text-muted);
        }

        .empty-icon {
            font-size: 36px;
            margin-bottom: 8px;
        }

        /* Footer info */
        footer {
            margin-top: 36px;
            padding-top: 20px;
            border-top: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 12px;
            color: var(--text-muted);
            flex-wrap: wrap;
            gap: 12px;
        }

        .code-path {
            font-family: 'JetBrains Mono', monospace;
            background: var(--surface-card);
            padding: 3px 8px;
            border-radius: 4px;
            border: 1px solid var(--border-color);
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-4px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>

<div class="container">
    <!-- Header -->
    <header>
        <div class="brand">
            <div class="brand-icon">🔥</div>
            <div class="brand-text">
                <h1>Calorie & Macro Logger</h1>
                <p>Enterprise JavaServer Page (JSP) Calorie Tracker</p>
            </div>
        </div>

        <div class="header-meta">
            <div class="badge-pill">
                <span class="dot"></span>
                <span>Storage: <strong style="color: #f8fafc;"><%= logFile != null ? logFile.getName() : "calorie-log.json" %></strong></span>
            </div>
            <div class="badge-pill">
                <span>Today: <strong style="color: #f8fafc;"><%= todayDate %></strong></span>
            </div>
        </div>
    </header>

    <!-- Notification Alert -->
    <% if (alertMessage != null) { %>
        <div class="alert <%= alertType %>">
            <div><%= alertMessage %></div>
            <button class="alert-close" onclick="this.parentElement.remove();">&times;</button>
        </div>
    <% } %>

    <!-- Analytics KPIs -->
    <div class="analytics-grid">
        <div class="stat-card cal">
            <div class="stat-title">
                <span>Today's Intake</span>
                <span>🔥</span>
            </div>
            <div class="stat-value"><%= String.format(Locale.US, "%.0f", totalCaloriesToday) %><span style="font-size: 14px; font-weight: 500; color: var(--text-muted);"> kcal</span></div>
            <div class="stat-subtext"><%= todayCount %> entries logged today</div>
        </div>

        <div class="stat-card carbs">
            <div class="stat-title">
                <span>Total Carbs</span>
                <span style="color: var(--accent-carb);">🍞</span>
            </div>
            <div class="stat-value"><%= String.format(Locale.US, "%.1f", totalCarbsAll) %><span style="font-size: 14px; font-weight: 500; color: var(--text-muted);"> g</span></div>
            <div class="stat-subtext"><%= String.format(Locale.US, "%.0f kcal from carbs (4 kcal/g)", totalCarbsAll * 4.0) %></div>
        </div>

        <div class="stat-card protein">
            <div class="stat-title">
                <span>Total Protein</span>
                <span style="color: var(--accent-protein);">🍗</span>
            </div>
            <div class="stat-value"><%= String.format(Locale.US, "%.1f", totalProteinAll) %><span style="font-size: 14px; font-weight: 500; color: var(--text-muted);"> g</span></div>
            <div class="stat-subtext"><%= String.format(Locale.US, "%.0f kcal from protein (4 kcal/g)", totalProteinAll * 4.0) %></div>
        </div>

        <div class="stat-card fats">
            <div class="stat-title">
                <span>Total Fats</span>
                <span style="color: var(--accent-fat);">🥑</span>
            </div>
            <div class="stat-value"><%= String.format(Locale.US, "%.1f", totalFatsAll) %><span style="font-size: 14px; font-weight: 500; color: var(--text-muted);"> g</span></div>
            <div class="stat-subtext"><%= String.format(Locale.US, "%.0f kcal from fats (9 kcal/g)", totalFatsAll * 9.0) %></div>
        </div>
    </div>

    <!-- Main Two-Column Layout -->
    <div class="main-layout">
        
        <!-- Left Column: Form -->
        <div class="panel">
            <div class="panel-header">
                <div class="panel-title"><span>➕</span> Log New Meal</div>
                <div class="panel-subtitle">Calculates calories: (Carbs &times; 4) + (Protein &times; 4) + (Fats &times; 9)</div>
            </div>

            <!-- Quick Presets -->
            <div class="presets-label">Quick Presets:</div>
            <div class="preset-chips">
                <button type="button" class="preset-btn" onclick="applyPreset('Oatmeal & Milk', 45, 12, 5)">🥣 Oatmeal</button>
                <button type="button" class="preset-btn" onclick="applyPreset('Chicken Breast & Rice', 48, 42, 6)">🍗 Chicken & Rice</button>
                <button type="button" class="preset-btn" onclick="applyPreset('Whey Protein Shake', 4, 30, 2)">🥤 Whey Shake</button>
                <button type="button" class="preset-btn" onclick="applyPreset('Eggs & Avocado Toast', 26, 16, 18)">🥑 Eggs & Toast</button>
            </div>

            <form action="calorieLogger.jsp" method="POST" id="calorieForm">
                <input type="hidden" name="action" value="add">

                <div class="form-group">
                    <label for="txtName">Meal / Food / Person Name</label>
                    <input type="text" id="txtName" name="name" placeholder="e.g. Grilled Chicken or Akshat" required autofocus>
                </div>

                <div class="form-group">
                    <label>Macronutrients (grams)</label>
                    <div class="input-row">
                        <div>
                            <input type="number" id="txtCarbs" name="carbs" value="0" min="0" step="0.5" placeholder="Carbs" oninput="updateLiveCalc()" required>
                            <span style="font-size: 11px; color: var(--accent-carb); font-weight: 600; display: block; margin-top: 3px;">Carbs (g)</span>
                        </div>
                        <div>
                            <input type="number" id="txtProtein" name="protein" value="0" min="0" step="0.5" placeholder="Protein" oninput="updateLiveCalc()" required>
                            <span style="font-size: 11px; color: var(--accent-protein); font-weight: 600; display: block; margin-top: 3px;">Protein (g)</span>
                        </div>
                        <div>
                            <input type="number" id="txtFats" name="fats" value="0" min="0" step="0.5" placeholder="Fats" oninput="updateLiveCalc()" required>
                            <span style="font-size: 11px; color: var(--accent-fat); font-weight: 600; display: block; margin-top: 3px;">Fats (g)</span>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <label for="txtDate">Log Date</label>
                    <input type="date" id="txtDate" name="date" value="<%= todayDate %>" required>
                </div>

                <!-- Live Macro Preview -->
                <div class="live-preview-box">
                    <div class="live-header">
                        <span>Live Calculation</span>
                        <div class="live-cals"><span id="previewCals">0.0</span> <span style="font-size: 12px; color: var(--text-muted);">kcal</span></div>
                    </div>

                    <div class="macro-ratio-bar">
                        <div id="barCarb" class="bar-carb" style="width: 33.3%;"></div>
                        <div id="barProtein" class="bar-protein" style="width: 33.3%;"></div>
                        <div id="barFat" class="bar-fat" style="width: 33.4%;"></div>
                    </div>

                    <div class="macro-legend">
                        <div class="legend-item"><span class="legend-dot dot-carb"></span> Carbs: <span id="pctCarb">33%</span></div>
                        <div class="legend-item"><span class="legend-dot dot-protein"></span> Protein: <span id="pctProtein">33%</span></div>
                        <div class="legend-item"><span class="legend-dot dot-fat"></span> Fat: <span id="pctFat">34%</span></div>
                    </div>
                </div>

                <button type="submit" class="btn-submit">
                    <span>💾</span> Save Entry to JSON
                </button>
            </form>
        </div>

        <!-- Right Column: History Table -->
        <div class="panel">
            <div class="table-controls">
                <div>
                    <div class="panel-title"><span>📋</span> Calorie Log History</div>
                    <div class="panel-subtitle"><%= logs.size() %> total entries recorded in database</div>
                </div>

                <!-- Filter / Search -->
                <form action="calorieLogger.jsp" method="GET" class="search-box">
                    <span class="search-icon">🔍</span>
                    <input type="text" name="search" placeholder="Search name or date..." value="<%= escapeHtml(searchQuery) %>">
                </form>
            </div>

            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Name / Meal</th>
                            <th>Carbs</th>
                            <th>Protein</th>
                            <th>Fats</th>
                            <th>Calories</th>
                            <th>Ratio</th>
                            <th style="text-align: right;">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            int displayedCount = 0;
                            // Display from newest to oldest
                            for (int i = logs.size() - 1; i >= 0; i--) {
                                CalorieLogItem item = logs.get(i);
                                
                                // Search filter check
                                if (!searchQuery.isEmpty()) {
                                    boolean matchName = item.getName().toLowerCase().contains(searchQuery.toLowerCase());
                                    boolean matchDate = item.getDate().toLowerCase().contains(searchQuery.toLowerCase());
                                    if (!matchName && !matchDate) continue;
                                }
                                displayedCount++;
                        %>
                            <tr>
                                <td class="cell-date"><%= escapeHtml(item.getDate()) %></td>
                                <td class="cell-name"><%= escapeHtml(item.getName()) %></td>
                                <td class="cell-num badge-carb"><%= String.format(Locale.US, "%.1fg", item.getCarbs()) %></td>
                                <td class="cell-num badge-protein"><%= String.format(Locale.US, "%.1fg", item.getProtein()) %></td>
                                <td class="cell-num badge-fat"><%= String.format(Locale.US, "%.1fg", item.getFats()) %></td>
                                <td>
                                    <span class="badge-cal"><%= String.format(Locale.US, "%.0f kcal", item.getTotalCalories()) %></span>
                                </td>
                                <td>
                                    <div class="mini-ratio" title="Carb: <%= item.getCarbPercent() %>%, Protein: <%= item.getProteinPercent() %>%, Fat: <%= item.getFatPercent() %>%">
                                        <div class="bar-carb" style="width: <%= item.getCarbPercent() %>%;"></div>
                                        <div class="bar-protein" style="width: <%= item.getProteinPercent() %>%;"></div>
                                        <div class="bar-fat" style="width: <%= item.getFatPercent() %>%;"></div>
                                    </div>
                                </td>
                                <td style="text-align: right;">
                                    <form action="calorieLogger.jsp" method="POST" style="display:inline;" onsubmit="return confirm('Delete log entry for <%= escapeHtml(item.getName()) %>?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="index" value="<%= i %>">
                                        <button type="submit" class="btn-delete" title="Delete entry">🗑️</button>
                                    </form>
                                </td>
                            </tr>
                        <%
                            }
                            if (displayedCount == 0) {
                        %>
                            <tr>
                                <td colspan="8">
                                    <div class="empty-state">
                                        <div class="empty-icon">🥗</div>
                                        <p><strong>No calorie logs found.</strong></p>
                                        <p style="font-size: 12px; margin-top: 4px;">
                                            <% if (!searchQuery.isEmpty()) { %>
                                                No results matching "<%= escapeHtml(searchQuery) %>". <a href="calorieLogger.jsp" style="color: var(--primary);">Clear filter</a>
                                            <% } else { %>
                                                Use the form on the left to add your first meal!
                                            <% } %>
                                        </p>
                                    </div>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

    <!-- Footer -->
    <footer>
        <div>
            Storage File: <span class="code-path"><%= logFile != null ? logFile.getAbsolutePath() : "calorie-log.json" %></span>
        </div>
        <div>
            Formula: <span class="code-path">(Carbs &times; 4) + (Protein &times; 4) + (Fats &times; 9)</span>
        </div>
    </footer>
</div>

<script>
    // Live calculation for preview box
    function updateLiveCalc() {
        const carbs = parseFloat(document.getElementById('txtCarbs').value) || 0;
        const protein = parseFloat(document.getElementById('txtProtein').value) || 0;
        const fats = parseFloat(document.getElementById('txtFats').value) || 0;

        const carbCals = carbs * 4.0;
        const proteinCals = protein * 4.0;
        const fatCals = fats * 9.0;
        const total = carbCals + proteinCals + fatCals;

        document.getElementById('previewCals').innerText = total.toFixed(1);

        if (total > 0) {
            const pctC = Math.round((carbCals / total) * 100);
            const pctP = Math.round((proteinCals / total) * 100);
            const pctF = Math.max(0, 100 - pctC - pctP);

            document.getElementById('barCarb').style.width = pctC + '%';
            document.getElementById('barProtein').style.width = pctP + '%';
            document.getElementById('barFat').style.width = pctF + '%';

            document.getElementById('pctCarb').innerText = pctC + '%';
            document.getElementById('pctProtein').innerText = pctP + '%';
            document.getElementById('pctFat').innerText = pctF + '%';
        } else {
            document.getElementById('barCarb').style.width = '33.3%';
            document.getElementById('barProtein').style.width = '33.3%';
            document.getElementById('barFat').style.width = '33.4%';

            document.getElementById('pctCarb').innerText = '0%';
            document.getElementById('pctProtein').innerText = '0%';
            document.getElementById('pctFat').innerText = '0%';
        }
    }

    // Apply quick preset
    function applyPreset(name, c, p, f) {
        document.getElementById('txtName').value = name;
        document.getElementById('txtCarbs').value = c;
        document.getElementById('txtProtein').value = p;
        document.getElementById('txtFats').value = f;
        updateLiveCalc();
    }

    // Initialize calculation on page load
    document.addEventListener('DOMContentLoaded', () => {
        updateLiveCalc();
    });
</script>

</body>
</html>
