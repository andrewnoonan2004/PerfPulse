#!/usr/bin/env python3
"""
PerfPulse Material 3 Expressive Screenshot Generator.
Renders high-resolution, pixel-perfect screenshots of the PerfPulse Android app
using Google Roboto typography and Material 3 Expressive palettes.
"""

import math
import os
from PIL import Image, ImageDraw, ImageFont

# Canvas dimensions
PHONE_WIDTH = 480
PHONE_HEIGHT = 1040

TABLET_WIDTH = 1100
TABLET_HEIGHT = 720

# Colors (Material 3 Expressive Dark Theme adapted to dynamic cyan/ice blue)
BG_COLOR = (17, 20, 24)          # surface
SURFACE_LOW = (25, 28, 33)       # surfaceContainerLow
SURFACE_HIGH = (33, 37, 43)      # surfaceContainerHigh
SURFACE_HIGHEST = (44, 49, 56)   # surfaceContainerHighest
OUTLINE_VARIANT = (64, 72, 78)   # outlineVariant

COLOR_PRIMARY = (135, 207, 249)          # #87CFF9 (Ice Blue)
COLOR_PRIMARY_CONTAINER = (0, 77, 104)   # #004D68
COLOR_ON_PRIMARY = (0, 52, 72)
COLOR_ON_PRIMARY_CONTAINER = (194, 232, 255)

COLOR_SECONDARY = (176, 202, 220)        # #B0CADC
COLOR_SECONDARY_CONTAINER = (52, 77, 92)

COLOR_TERTIARY = (230, 181, 248)         # #E6B5F8 (Lilac / Lavender)
COLOR_TERTIARY_CONTAINER = (90, 51, 108)

TEXT_HIGH = (226, 226, 230)              # onSurface
TEXT_MED = (176, 182, 192)               # onSurfaceVariant
TEXT_LOW = (120, 126, 136)

STATUS_GREEN = (94, 218, 142)
STATUS_AMBER = (255, 185, 70)
STATUS_RED = (255, 120, 110)

# Fonts
FONT_DIR = "/usr/share/fonts/truetype/roboto/unhinted/RobotoTTF"
def get_font(name="Roboto-Regular.ttf", size=14):
    path = os.path.join(FONT_DIR, name)
    if os.path.exists(path):
        return ImageFont.truetype(path, size)
    return ImageFont.load_default()

font_title_hero = get_font("Roboto-Bold.ttf", 26)
font_title_lg = get_font("Roboto-Bold.ttf", 20)
font_title_md = get_font("Roboto-Bold.ttf", 16)
font_title_sm = get_font("Roboto-Medium.ttf", 14)
font_body = get_font("Roboto-Regular.ttf", 13)
font_body_bold = get_font("Roboto-Bold.ttf", 13)
font_label = get_font("Roboto-Medium.ttf", 11)
font_label_sm = get_font("Roboto-Regular.ttf", 10)
font_code = get_font("Roboto-Medium.ttf", 12)
font_big_gauge = get_font("Roboto-Bold.ttf", 36)
font_gauge_val = get_font("Roboto-Bold.ttf", 28)

def draw_device_frame(draw, w, h, radius=32):
    draw.rounded_rectangle([0, 0, w - 1, h - 1], radius=radius, outline=(55, 62, 70), width=2)

def draw_status_bar(draw, w):
    time_str = "10:42"
    draw.text((26, 11), time_str, fill=TEXT_HIGH, font=font_body_bold)
    
    rx = w - 85
    draw.text((rx, 12), "5G", fill=TEXT_MED, font=font_label_sm)
    
    wx = rx + 24
    draw.arc([wx, 13, wx + 15, 25], 200, 340, fill=TEXT_MED, width=2)
    draw.arc([wx + 3, 17, wx + 12, 25], 200, 340, fill=TEXT_MED, width=2)
    draw.point((wx + 7, 21), fill=TEXT_MED)
    
    bx = rx + 48
    draw.rounded_rectangle([bx, 13, bx + 22, 23], radius=3, outline=TEXT_MED, width=1)
    draw.rectangle([bx + 2, 15, bx + 18, 21], fill=STATUS_GREEN)
    draw.rectangle([bx + 22, 16, bx + 24, 20], fill=TEXT_MED)

def draw_top_app_bar(draw, w, title="PerfPulse", subtitle="Material 3 Expressive • 1000ms"):
    y = 36
    cx, cy, cr = 42, y + 20, 18
    cookie_pts = []
    for i in range(24):
        ang = i * (2 * math.pi / 24)
        r = cr if i % 2 == 0 else cr * 0.82
        cookie_pts.append((cx + r * math.cos(ang), cy + r * math.sin(ang)))
    draw.polygon(cookie_pts, fill=COLOR_PRIMARY_CONTAINER)
    
    draw.arc([cx - 9, cy - 9, cx + 9, cy + 9], 150, 390, fill=COLOR_PRIMARY, width=2)
    draw.line([cx, cy, cx + 5, cy - 5], fill=COLOR_PRIMARY, width=2)
    draw.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=COLOR_PRIMARY)
    
    draw.text((70, y + 4), title, fill=TEXT_HIGH, font=font_title_md)
    draw.text((70, y + 24), subtitle, fill=COLOR_PRIMARY, font=font_label_sm)
    
    rw, rh = 62, 26
    rx = w - rw - 18
    ry = y + 7
    draw.rounded_rectangle([rx, ry, rx + rw, ry + rh], radius=13, fill=SURFACE_HIGH, outline=OUTLINE_VARIANT, width=1)
    draw.text((rx + 11, ry + 6), "1000ms", fill=TEXT_HIGH, font=font_label)
    
    px = rx - 34
    draw.rounded_rectangle([px, ry, px + 26, ry + 26], radius=13, fill=SURFACE_HIGH, outline=OUTLINE_VARIANT, width=1)
    draw.rectangle([px + 8, ry + 7, px + 11, ry + 19], fill=TEXT_MED)
    draw.rectangle([px + 15, ry + 7, px + 18, ry + 19], fill=TEXT_MED)

def draw_bottom_nav(draw, w, h, selected_idx=0):
    nav_h = 74
    y = h - nav_h - 16
    draw.rectangle([0, y, w, h], fill=SURFACE_LOW)
    draw.line([0, y, w, y], fill=(38, 42, 48), width=1)
    
    items = ["Overview", "CPU", "GPU", "RAM", "Processes"]
    item_w = w / len(items)
    
    for idx, label in enumerate(items):
        ix = idx * item_w + item_w / 2
        is_sel = (idx == selected_idx)
        
        if is_sel:
            pw, ph = 56, 30
            draw.rounded_rectangle([ix - pw/2, y + 8, ix + pw/2, y + 8 + ph], radius=15, fill=COLOR_PRIMARY_CONTAINER)
        
        ic_y = y + 16
        col = COLOR_ON_PRIMARY_CONTAINER if is_sel else TEXT_MED
        
        if idx == 0:
            draw.rectangle([ix - 9, ic_y - 6, ix - 2, ic_y + 1], outline=col, width=1)
            draw.rectangle([ix + 2, ic_y - 6, ix + 9, ic_y + 6], outline=col, width=1)
            draw.rectangle([ix - 9, ic_y + 4, ix - 2, ic_y + 9], outline=col, width=1)
        elif idx == 1:
            draw.rounded_rectangle([ix - 8, ic_y - 7, ix + 8, ic_y + 7], radius=2, outline=col, width=1)
            draw.rectangle([ix - 4, ic_y - 3, ix + 4, ic_y + 3], fill=col)
            for p in [-5, 0, 5]:
                draw.line([ix + p, ic_y - 9, ix + p, ic_y - 7], fill=col, width=1)
                draw.line([ix + p, ic_y + 7, ix + p, ic_y + 9], fill=col, width=1)
        elif idx == 2:
            draw.rounded_rectangle([ix - 9, ic_y - 6, ix + 9, ic_y + 6], radius=2, outline=col, width=1)
            draw.ellipse([ix - 5, ic_y - 3, ix + 1, ic_y + 3], outline=col, width=1)
            draw.ellipse([ix + 1, ic_y - 3, ix + 7, ic_y + 3], outline=col, width=1)
        elif idx == 3:
            draw.rectangle([ix - 8, ic_y - 5, ix + 8, ic_y + 6], outline=col, width=1)
            for c in range(4):
                cx = ix - 6 + c * 3.5
                draw.line([cx, ic_y - 1, cx, ic_y + 2], fill=col, width=1)
        elif idx == 4:
            draw.line([ix - 7, ic_y - 4, ix + 7, ic_y - 4], fill=col, width=2)
            draw.line([ix - 7, ic_y, ix + 7, ic_y], fill=col, width=2)
            draw.line([ix - 7, ic_y + 4, ix + 7, ic_y + 4], fill=col, width=2)
            
        lbl_col = COLOR_PRIMARY if is_sel else TEXT_MED
        font = font_label if is_sel else font_label_sm
        bbox = draw.textbbox((0, 0), label, font=font)
        tw = bbox[2] - bbox[0]
        draw.text((ix - tw / 2, y + 43), label, fill=lbl_col, font=font)
        
    draw.rounded_rectangle([w/2 - 40, h - 10, w/2 + 40, h - 6], radius=2, fill=(120, 126, 134))

def draw_wavy_circle(draw, cx, cy, radius, percent, color, bg_color=(45, 52, 60), waves=12, amp=3):
    draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius], outline=bg_color, width=6)
    
    arc_points = []
    total_steps = 240
    end_step = int(total_steps * (percent / 100.0))
    start_angle = -math.pi / 2
    
    for i in range(end_step + 1):
        theta = start_angle + (2 * math.pi * (i / total_steps))
        wave_offset = amp * math.sin(waves * theta)
        r = radius + wave_offset
        px = cx + r * math.cos(theta)
        py = cy + r * math.sin(theta)
        arc_points.append((px, py))
        
    if len(arc_points) > 1:
        for i in range(len(arc_points) - 1):
            draw.line([arc_points[i], arc_points[i+1]], fill=color, width=6)

def draw_wavy_linear_bar(draw, x, y, w, h, percent, color, bg_color=(45, 52, 60), waves=6, amp=2.5):
    draw.rounded_rectangle([x, y, x + w, y + h], radius=h/2, fill=bg_color)
    fill_w = int(w * (percent / 100.0))
    if fill_w < 4:
        return
    pts = []
    steps = max(20, int(fill_w / 2))
    for i in range(steps + 1):
        px = x + (i / steps) * fill_w
        py = y + h / 2 + amp * math.sin(waves * (i / steps) * 2 * math.pi)
        pts.append((px, py))
    for i in range(len(pts) - 1):
        draw.line([pts[i], pts[i+1]], fill=color, width=int(h * 0.75))

def draw_sparkline(draw, x, y, w, h, data, stroke_color, fill_color):
    if not data or len(data) < 2:
        return
    min_v, max_v = 0, 100
    pts = []
    dx = w / (len(data) - 1)
    for i, val in enumerate(data):
        normalized = (val - min_v) / (max_v - min_v)
        py = y + h - (normalized * h)
        pts.append((x + i * dx, py))
        
    poly_pts = [(pts[0][0], y + h)] + pts + [(pts[-1][0], y + h)]
    draw.polygon(poly_pts, fill=fill_color)
    
    for i in range(len(pts) - 1):
        draw.line([pts[i], pts[i+1]], fill=stroke_color, width=2)
        
    lx, ly = pts[-1]
    draw.ellipse([lx - 3, ly - 3, lx + 3, ly + 3], fill=stroke_color)

def draw_expressive_shape(draw, cx, cy, r, shape_type, fill_color):
    pts = []
    if shape_type == "cookie":
        for i in range(24):
            ang = i * (2 * math.pi / 24)
            rad = r if i % 2 == 0 else r * 0.82
            pts.append((cx + rad * math.cos(ang), cy + rad * math.sin(ang)))
    elif shape_type == "sunny":
        for i in range(16):
            ang = i * (2 * math.pi / 16)
            rad = r if i % 2 == 0 else r * 0.72
            pts.append((cx + rad * math.cos(ang), cy + rad * math.sin(ang)))
    elif shape_type == "clover":
        for i in range(32):
            ang = i * (2 * math.pi / 32)
            rad = r * (0.85 + 0.15 * math.cos(4 * ang))
            pts.append((cx + rad * math.cos(ang), cy + rad * math.sin(ang)))
    else:
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=fill_color)
        return
    draw.polygon(pts, fill=fill_color)

def draw_gauge_card(draw, x, y, w, h, title, subtitle, percent, detail, shape_type, color, shape_bg):
    draw.rounded_rectangle([x, y, x + w, y + h], radius=22, fill=SURFACE_HIGH)
    
    # Header shape
    sx, sy = x + 30, y + 28
    draw_expressive_shape(draw, sx, sy, 16, shape_type, shape_bg)
    
    # Title & Subtitle
    draw.text((x + 56, y + 16), title, fill=TEXT_HIGH, font=font_title_sm)
    draw.text((x + 56, y + 33), subtitle, fill=TEXT_MED, font=font_label_sm)
    
    # Wavy gauge in center right
    gx, gy = x + w - 46, y + 42
    draw_wavy_circle(draw, gx, gy, radius=24, percent=percent, color=color, amp=2, waves=10)
    pct_text = f"{int(percent)}%"
    bbox = draw.textbbox((0, 0), pct_text, font=font_body_bold)
    pw = bbox[2] - bbox[0]
    draw.text((gx - pw/2, gy - 8), pct_text, fill=TEXT_HIGH, font=font_body_bold)
    
    # Detail bottom text
    draw.text((x + 18, y + h - 22), detail, fill=TEXT_LOW, font=font_label_sm)


# ==========================================
# 1. OVERVIEW SCREEN
# ==========================================
def render_overview():
    im = Image.new("RGB", (PHONE_WIDTH, PHONE_HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(im)
    
    draw_status_bar(draw, PHONE_WIDTH)
    draw_top_app_bar(draw, PHONE_WIDTH, "PerfPulse", "Material 3 Expressive • 1000ms")
    
    y = 86
    card_w = PHONE_WIDTH - 32
    
    # Card 1: CPU
    draw_gauge_card(
        draw, 16, y, card_w, 94,
        title="CPU", subtitle="Google Tensor G4",
        percent=42.0, detail="8/8 Cores Online • frequency",
        shape_type="cookie", color=COLOR_PRIMARY, shape_bg=COLOR_PRIMARY_CONTAINER
    )
    y += 104
    
    # Card 2: GPU
    draw_gauge_card(
        draw, 16, y, card_w, 94,
        title="GPU", subtitle="Mali-G715 Immortalis MC10",
        percent=35.0, detail="700 MHz • Hardware Telemetry",
        shape_type="sunny", color=COLOR_TERTIARY, shape_bg=COLOR_TERTIARY_CONTAINER
    )
    y += 104
    
    # Card 3: RAM
    draw_gauge_card(
        draw, 16, y, card_w, 94,
        title="RAM", subtitle="7.8 GB / 15.4 GB used",
        percent=51.0, detail="Swap/ZRAM: 1.2 GB • Free: 7.6 GB",
        shape_type="clover", color=COLOR_SECONDARY, shape_bg=COLOR_SECONDARY_CONTAINER
    )
    y += 104
    
    # Card 4: Live Activity Telemetry (Sparklines)
    card_h = 296
    draw.rounded_rectangle([16, y, 16 + card_w, y + card_h], radius=24, fill=SURFACE_HIGH)
    draw.text((32, y + 16), "Live Activity Telemetry", fill=TEXT_HIGH, font=font_title_sm)
    draw.text((32, y + 34), "Real-time rolling history (last 60s)", fill=TEXT_MED, font=font_label_sm)
    
    # Status badge
    bx = 16 + card_w - 78
    draw.rounded_rectangle([bx, y + 16, bx + 62, y + 36], radius=10, fill=(35, 60, 45))
    draw.text((bx + 11, y + 21), "LIVE", fill=STATUS_GREEN, font=font_label)
    
    sy = y + 56
    # CPU Stream
    draw.text((32, sy), "CPU Load: 42.0%", fill=COLOR_PRIMARY, font=font_body_bold)
    cpu_data = [18, 22, 28, 35, 48, 52, 45, 38, 42, 50, 62, 55, 40, 36, 42, 45, 39, 42]
    draw_sparkline(draw, 32, sy + 18, card_w - 32, 44, cpu_data, COLOR_PRIMARY, (15, 45, 65, 80))
    
    sy += 76
    # GPU Stream
    draw.text((32, sy), "GPU Load: 35.0%", fill=COLOR_TERTIARY, font=font_body_bold)
    gpu_data = [10, 15, 20, 25, 45, 60, 52, 30, 22, 28, 35, 40, 48, 32, 28, 35]
    draw_sparkline(draw, 32, sy + 18, card_w - 32, 44, gpu_data, COLOR_TERTIARY, (45, 25, 55, 80))
    
    sy += 76
    # RAM Stream
    draw.text((32, sy), "RAM Usage: 51.0%", fill=COLOR_SECONDARY, font=font_body_bold)
    ram_data = [49, 49, 50, 50, 50, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51]
    draw_sparkline(draw, 32, sy + 18, card_w - 32, 44, ram_data, COLOR_SECONDARY, (35, 45, 55, 80))
    
    y += card_h + 12
    
    # Card 5: Thermal & System Vitals
    vitals_h = 138
    draw.rounded_rectangle([16, y, 16 + card_w, y + vitals_h], radius=24, fill=SURFACE_HIGH)
    draw.text((32, y + 16), "Thermal & System Vitals", fill=TEXT_HIGH, font=font_title_sm)
    
    # 2x2 Tiles
    tile_w = (card_w - 40) / 2
    # Row 1
    t1_x, t1_y = 30, y + 42
    draw.rounded_rectangle([t1_x, t1_y, t1_x + tile_w, t1_y + 40], radius=14, fill=SURFACE_LOW)
    draw.text((t1_x + 10, t1_y + 6), "Battery Temp", fill=TEXT_LOW, font=font_label_sm)
    draw.text((t1_x + 10, t1_y + 19), "34.2 °C • Nominal", fill=TEXT_HIGH, font=font_body_bold)
    
    t2_x = t1_x + tile_w + 12
    draw.rounded_rectangle([t2_x, t1_y, t2_x + tile_w, t1_y + 40], radius=14, fill=SURFACE_LOW)
    draw.text((t2_x + 10, t1_y + 6), "Headroom", fill=TEXT_LOW, font=font_label_sm)
    draw.text((t2_x + 10, t1_y + 19), "1.00x Optimal", fill=TEXT_HIGH, font=font_body_bold)
    
    # Row 2
    t3_y = t1_y + 46
    draw.rounded_rectangle([t1_x, t3_y, t1_x + tile_w, t3_y + 40], radius=14, fill=SURFACE_LOW)
    draw.text((t1_x + 10, t3_y + 6), "Active Apps", fill=TEXT_LOW, font=font_label_sm)
    draw.text((t1_x + 10, t3_y + 19), "18 Recent", fill=TEXT_HIGH, font=font_body_bold)
    
    draw.rounded_rectangle([t2_x, t3_y, t2_x + tile_w, t3_y + 40], radius=14, fill=SURFACE_LOW)
    draw.text((t2_x + 10, t3_y + 6), "Monitor Uptime", fill=TEXT_LOW, font=font_label_sm)
    draw.text((t2_x + 10, t3_y + 19), "4m 12s • PID 12480", fill=TEXT_HIGH, font=font_body_bold)
    
    draw_bottom_nav(draw, PHONE_WIDTH, PHONE_HEIGHT, selected_idx=0)
    draw_device_frame(draw, PHONE_WIDTH, PHONE_HEIGHT)
    return im


# ==========================================
# 2. CPU SCREEN
# ==========================================
def render_cpu():
    im = Image.new("RGB", (PHONE_WIDTH, PHONE_HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(im)
    
    draw_status_bar(draw, PHONE_WIDTH)
    draw_top_app_bar(draw, PHONE_WIDTH, "CPU Telemetry", "8 Cores • 3 Clusters")
    
    y = 86
    card_w = PHONE_WIDTH - 32
    
    # Hero CPU Card with Big Wavy Circular Gauge
    hero_h = 160
    draw.rounded_rectangle([16, y, 16 + card_w, y + hero_h], radius=24, fill=SURFACE_HIGH)
    
    # Left gauge
    gx, gy = 76, y + hero_h/2
    draw_wavy_circle(draw, gx, gy, radius=46, percent=48.0, color=COLOR_PRIMARY, amp=3.5, waves=12)
    draw.text((gx - 26, gy - 16), "48%", fill=TEXT_HIGH, font=font_gauge_val)
    draw.text((gx - 18, gy + 12), "Total", fill=TEXT_MED, font=font_label_sm)
    
    # Right info
    rx = 144
    draw.text((rx, y + 20), "Google Tensor G4", fill=TEXT_HIGH, font=font_title_md)
    draw.text((rx, y + 42), "ARMv9-A • 64-bit Architecture", fill=COLOR_PRIMARY, font=font_label)
    draw.text((rx, y + 62), "Governor: schedutil (energy-aware)", fill=TEXT_MED, font=font_body)
    draw.text((rx, y + 82), "ABIs: arm64-v8a", fill=TEXT_MED, font=font_body)
    draw.text((rx, y + 106), "App Overhead: 2.1% CPU • PID: 12480", fill=TEXT_LOW, font=font_label_sm)
    
    y += hero_h + 12
    
    # Sparkline History Card
    spk_h = 100
    draw.rounded_rectangle([16, y, 16 + card_w, y + spk_h], radius=20, fill=SURFACE_HIGH)
    draw.text((32, y + 14), "CPU Load Waveform (Last 60s)", fill=TEXT_HIGH, font=font_body_bold)
    cpu_data = [20, 24, 30, 42, 68, 55, 38, 44, 52, 60, 48, 42, 50, 48]
    draw_sparkline(draw, 32, y + 36, card_w - 32, 48, cpu_data, COLOR_PRIMARY, (15, 45, 65, 80))
    
    y += spk_h + 12
    
    # Clusters Overview Banner
    cl_h = 74
    draw.rounded_rectangle([16, y, 16 + card_w, y + cl_h], radius=20, fill=SURFACE_HIGH)
    draw.text((32, y + 12), "Tri-Cluster Architecture", fill=TEXT_HIGH, font=font_body_bold)
    
    cw = (card_w - 48) / 3
    for ci, (cname, cdetail, cfreq) in enumerate([
        ("LITTLE (4x)", "Cortex-A520", "1.95 GHz"),
        ("MID (3x)", "Cortex-A725", "2.60 GHz"),
        ("BIG (1x)", "Cortex-X4", "3.10 GHz")
    ]):
        cx = 32 + ci * (cw + 12)
        draw.rounded_rectangle([cx, y + 32, cx + cw, y + 62], radius=10, fill=SURFACE_LOW)
        draw.text((cx + 8, y + 35), cname, fill=COLOR_PRIMARY, font=font_label_sm)
        draw.text((cx + 8, y + 47), cfreq, fill=TEXT_HIGH, font=font_body_bold)
        
    y += cl_h + 12
    
    # Per-core real-time gauges list (4 sample cores)
    core_list = [
        ("Core 0", "LITTLE • Cortex-A520", "1320 MHz / 1950 MHz", 32.0, "schedutil"),
        ("Core 1", "LITTLE • Cortex-A520", "1440 MHz / 1950 MHz", 41.0, "schedutil"),
        ("Core 4", "MID • Cortex-A725", "2100 MHz / 2600 MHz", 55.0, "schedutil"),
        ("Core 7", "BIG • Cortex-X4 (Prime)", "2850 MHz / 3100 MHz", 84.0, "schedutil")
    ]
    
    for core_name, arch, freq_str, pct, gov in core_list:
        ch = 72
        draw.rounded_rectangle([16, y, 16 + card_w, y + ch], radius=18, fill=SURFACE_HIGH)
        
        # Core icon circle
        draw.rounded_rectangle([28, y + 16, 68, y + 56], radius=12, fill=SURFACE_LOW)
        draw.text((34, y + 26), core_name.split()[1], fill=COLOR_PRIMARY, font=font_title_sm)
        
        # Labels
        draw.text((78, y + 14), f"{core_name} ({arch})", fill=TEXT_HIGH, font=font_body_bold)
        draw.text((78, y + 30), f"{freq_str} • {gov}", fill=TEXT_MED, font=font_label_sm)
        
        # Wavy Linear progress
        draw_wavy_linear_bar(draw, 78, y + 46, card_w - 150, 10, pct, COLOR_PRIMARY, amp=1.8)
        
        # Percentage on right
        col = STATUS_RED if pct > 80 else (STATUS_AMBER if pct > 50 else COLOR_PRIMARY)
        pct_str = f"{int(pct)}%"
        draw.text((16 + card_w - 48, y + 26), pct_str, fill=col, font=font_body_bold)
        
        y += ch + 8
        
    draw_bottom_nav(draw, PHONE_WIDTH, PHONE_HEIGHT, selected_idx=1)
    draw_device_frame(draw, PHONE_WIDTH, PHONE_HEIGHT)
    return im


# ==========================================
# 3. GPU SCREEN
# ==========================================
def render_gpu():
    im = Image.new("RGB", (PHONE_WIDTH, PHONE_HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(im)
    
    draw_status_bar(draw, PHONE_WIDTH)
    draw_top_app_bar(draw, PHONE_WIDTH, "GPU Telemetry", "Hardware Driver & APIs")
    
    y = 86
    card_w = PHONE_WIDTH - 32
    
    # Hero GPU Card
    hero_h = 160
    draw.rounded_rectangle([16, y, 16 + card_w, y + hero_h], radius=24, fill=SURFACE_HIGH)
    
    gx, gy = 76, y + hero_h/2
    draw_wavy_circle(draw, gx, gy, radius=46, percent=38.0, color=COLOR_TERTIARY, amp=3.5, waves=12)
    draw.text((gx - 26, gy - 16), "38%", fill=TEXT_HIGH, font=font_gauge_val)
    draw.text((gx - 18, gy + 12), "Load", fill=TEXT_MED, font=font_label_sm)
    
    rx = 144
    draw.text((rx, y + 20), "Mali-G715 Immortalis", fill=TEXT_HIGH, font=font_title_md)
    draw.text((rx, y + 42), "ARM • 10 Core Sub-system", fill=COLOR_TERTIARY, font=font_label)
    draw.text((rx, y + 62), "Clock: 685 MHz (Range: 300-890)", fill=TEXT_MED, font=font_body)
    draw.text((rx, y + 82), "Source: /sys/kernel/gpu/gpu_busy", fill=TEXT_MED, font=font_label_sm)
    draw.text((rx, y + 106), "Driver Telemetry: Hardware Direct", fill=TEXT_LOW, font=font_label_sm)
    
    y += hero_h + 14
    
    # GPU Sparkline Card
    spk_h = 120
    draw.rounded_rectangle([16, y, 16 + card_w, y + spk_h], radius=20, fill=SURFACE_HIGH)
    draw.text((32, y + 16), "GPU Utilization & Frequency (Last 60s)", fill=TEXT_HIGH, font=font_body_bold)
    gpu_data = [12, 18, 25, 40, 62, 54, 38, 28, 35, 48, 52, 40, 32, 38]
    draw_sparkline(draw, 32, y + 42, card_w - 32, 58, gpu_data, COLOR_TERTIARY, (45, 25, 55, 80))
    
    y += spk_h + 14
    
    # Graphics APIs & Capabilities Card
    api_h = 170
    draw.rounded_rectangle([16, y, 16 + card_w, y + api_h], radius=22, fill=SURFACE_HIGH)
    draw.text((32, y + 16), "Graphics Subsystem Capabilities", fill=TEXT_HIGH, font=font_title_sm)
    
    apis = [
        ("Vulkan API", "1.3.275 (Hardware Ray Tracing Enabled)", STATUS_GREEN),
        ("OpenGL ES", "Version 3.2 v1.r38p0-01eac0", STATUS_GREEN),
        ("Shader Subgroup", "Supported (Quad & Cluster Size 16)", COLOR_PRIMARY),
        ("Variable Rate Shading", "Tier 2 VRS Hardware Accelerated", COLOR_TERTIARY),
    ]
    
    ay = y + 44
    for title, val, col in apis:
        draw.text((32, ay), title, fill=TEXT_MED, font=font_label)
        draw.text((32, ay + 14), val, fill=TEXT_HIGH, font=font_body_bold)
        ay += 29
        
    y += api_h + 14
    
    # Frequency Scaling & Diode Temperature Card
    freq_h = 120
    draw.rounded_rectangle([16, y, 16 + card_w, y + freq_h], radius=22, fill=SURFACE_HIGH)
    draw.text((32, y + 16), "Dynamic Frequency Scaling", fill=TEXT_HIGH, font=font_title_sm)
    
    draw.text((32, y + 42), "Current Clock: 685 MHz (65% of Max)", fill=TEXT_MED, font=font_body)
    draw_wavy_linear_bar(draw, 32, y + 62, card_w - 32, 12, 65.0, COLOR_TERTIARY, amp=2.2)
    
    draw.text((32, y + 84), "Min: 300 MHz", fill=TEXT_LOW, font=font_label_sm)
    draw.text((16 + card_w - 110, y + 84), "Max: 890 MHz", fill=TEXT_LOW, font=font_label_sm)
    draw.text((16 + card_w/2 - 40, y + 84), "GPU Diode: 38.5 °C", fill=STATUS_GREEN, font=font_label_sm)
    
    draw_bottom_nav(draw, PHONE_WIDTH, PHONE_HEIGHT, selected_idx=2)
    draw_device_frame(draw, PHONE_WIDTH, PHONE_HEIGHT)
    return im


# ==========================================
# 4. RAM / MEMORY SCREEN
# ==========================================
def render_ram():
    im = Image.new("RGB", (PHONE_WIDTH, PHONE_HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(im)
    
    draw_status_bar(draw, PHONE_WIDTH)
    draw_top_app_bar(draw, PHONE_WIDTH, "Memory & ZRAM", "16 GB LPDDR5X")
    
    y = 86
    card_w = PHONE_WIDTH - 32
    
    # Hero Memory Card
    hero_h = 160
    draw.rounded_rectangle([16, y, 16 + card_w, y + hero_h], radius=24, fill=SURFACE_HIGH)
    
    gx, gy = 76, y + hero_h/2
    draw_wavy_circle(draw, gx, gy, radius=46, percent=51.0, color=COLOR_SECONDARY, amp=3.5, waves=12)
    draw.text((gx - 26, gy - 16), "51%", fill=TEXT_HIGH, font=font_gauge_val)
    draw.text((gx - 18, gy + 12), "Used", fill=TEXT_MED, font=font_label_sm)
    
    rx = 144
    draw.text((rx, y + 20), "7.85 GB / 15.42 GB", fill=TEXT_HIGH, font=font_title_md)
    draw.text((rx, y + 42), "LPDDR5X Quad-Channel 4266MHz", fill=COLOR_SECONDARY, font=font_label)
    draw.text((rx, y + 62), "Available: 7.57 GB • Free: 2.15 GB", fill=TEXT_MED, font=font_body)
    draw.text((rx, y + 82), "Low Mem Threshold: 1.00 GB", fill=TEXT_MED, font=font_body)
    draw.text((rx, y + 106), "Pressure Status: Healthy (No Trimming)", fill=STATUS_GREEN, font=font_label_sm)
    
    y += hero_h + 14
    
    # Kernel Memory Distribution Card
    dist_h = 180
    draw.rounded_rectangle([16, y, 16 + card_w, y + dist_h], radius=22, fill=SURFACE_HIGH)
    draw.text((32, y + 16), "Kernel Memory Allocations (/proc/meminfo)", fill=TEXT_HIGH, font=font_title_sm)
    
    # 2x3 Grid of stats
    stats = [
        ("Active Memory", "5.42 GB"),
        ("Inactive Memory", "2.43 GB"),
        ("Page Cache", "4.12 GB"),
        ("Kernel Buffers", "420 MB"),
        ("Anonymous / PSS", "4.85 GB"),
        ("Shared (shmem)", "180 MB"),
    ]
    
    col_w = (card_w - 40) / 2
    for si, (sname, sval) in enumerate(stats):
        col = si % 2
        row = si // 2
        bx = 32 + col * (col_w + 12)
        by = y + 44 + row * 40
        draw.rounded_rectangle([bx, by, bx + col_w, by + 34], radius=10, fill=SURFACE_LOW)
        draw.text((bx + 10, by + 4), sname, fill=TEXT_LOW, font=font_label_sm)
        draw.text((bx + 10, by + 16), sval, fill=TEXT_HIGH, font=font_body_bold)
        
    y += dist_h + 14
    
    # ZRAM / Swap Telemetry Card
    swap_h = 120
    draw.rounded_rectangle([16, y, 16 + card_w, y + swap_h], radius=22, fill=SURFACE_HIGH)
    draw.text((32, y + 16), "ZRAM Swap Compression", fill=TEXT_HIGH, font=font_title_sm)
    
    draw.text((32, y + 42), "Swap Used: 1.18 GB / 4.00 GB (29%)", fill=TEXT_MED, font=font_body)
    draw_wavy_linear_bar(draw, 32, y + 62, card_w - 32, 12, 29.5, COLOR_SECONDARY, amp=2.2)
    
    draw.text((32, y + 84), "Algorithm: lz4 / zstd", fill=TEXT_LOW, font=font_label_sm)
    draw.text((16 + card_w - 140, y + 84), "Compression Ratio: 3.2x", fill=COLOR_SECONDARY, font=font_label_sm)
    
    y += swap_h + 14
    
    # Memory Pressure Sparkline
    spk_h = 90
    draw.rounded_rectangle([16, y, 16 + card_w, y + spk_h], radius=20, fill=SURFACE_HIGH)
    draw.text((32, y + 14), "Memory Pressure History", fill=TEXT_HIGH, font=font_body_bold)
    mem_data = [50, 50, 50, 51, 51, 51, 51, 52, 51, 51, 51, 51]
    draw_sparkline(draw, 32, y + 34, card_w - 32, 42, mem_data, COLOR_SECONDARY, (35, 45, 55, 80))
    
    draw_bottom_nav(draw, PHONE_WIDTH, PHONE_HEIGHT, selected_idx=3)
    draw_device_frame(draw, PHONE_WIDTH, PHONE_HEIGHT)
    return im


# ==========================================
# 5. PROCESSES SCREEN
# ==========================================
def render_processes():
    im = Image.new("RGB", (PHONE_WIDTH, PHONE_HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(im)
    
    draw_status_bar(draw, PHONE_WIDTH)
    draw_top_app_bar(draw, PHONE_WIDTH, "Process Telemetry", "Runtime Vitals & Apps")
    
    y = 86
    card_w = PHONE_WIDTH - 32
    
    # Own Process Card
    own_h = 174
    draw.rounded_rectangle([16, y, 16 + card_w, y + own_h], radius=24, fill=SURFACE_HIGH)
    
    draw.text((32, y + 16), "PerfPulse Runtime Vitals", fill=TEXT_HIGH, font=font_title_sm)
    draw.text((32, y + 34), "com.droid.perfpulse • PID: 12480 • UID: 10245", fill=COLOR_PRIMARY, font=font_label_sm)
    
    # 4 stats
    pstats = [
        ("Total PSS Memory", "46.2 MB"),
        ("Java / Native Heap", "21.4 MB / 18.2 MB"),
        ("Active Threads", "14 Worker Threads"),
        ("CPU Time / Uptime", "1.84 s / 4m 32s"),
    ]
    
    col_w = (card_w - 40) / 2
    for si, (sname, sval) in enumerate(pstats):
        col = si % 2
        row = si // 2
        bx = 32 + col * (col_w + 12)
        by = y + 56 + row * 52
        draw.rounded_rectangle([bx, by, bx + col_w, by + 44], radius=12, fill=SURFACE_LOW)
        draw.text((bx + 10, by + 6), sname, fill=TEXT_LOW, font=font_label_sm)
        draw.text((bx + 10, by + 22), sval, fill=TEXT_HIGH, font=font_body_bold)
        
    y += own_h + 14
    
    # Apps List Header & Status Chip
    draw.text((20, y + 6), "Active System & User Apps", fill=TEXT_HIGH, font=font_title_sm)
    
    badge_w = 120
    bx = 16 + card_w - badge_w
    draw.rounded_rectangle([bx, y + 2, bx + badge_w, y + 24], radius=11, fill=(25, 45, 35))
    draw.text((bx + 12, y + 6), "Usage Access: Active", fill=STATUS_GREEN, font=font_label_sm)
    
    y += 34
    
    # List of running apps
    apps = [
        ("Google Chrome", "com.android.chrome", "320 MB PSS", "Foreground: 12m 45s", STATUS_GREEN, "Foreground"),
        ("Spotify", "com.spotify.music", "185 MB PSS", "Background Audio: 45m", COLOR_PRIMARY, "Service"),
        ("System UI", "com.android.systemui", "142 MB PSS", "Core OS Compositor", TEXT_MED, "System"),
        ("YouTube", "com.google.android.youtube", "248 MB PSS", "Last active: 8m ago", COLOR_TERTIARY, "Background"),
        ("Terminal", "com.android.virtualization.terminal", "98 MB PSS", "VM Host Service", COLOR_SECONDARY, "Service"),
        ("Android Messages", "com.google.android.apps.messaging", "86 MB PSS", "Last active: 22m ago", TEXT_MED, "Background"),
    ]
    
    for label, pkg, pss, duration, tag_col, tag in apps:
        app_h = 58
        draw.rounded_rectangle([16, y, 16 + card_w, y + app_h], radius=16, fill=SURFACE_HIGH)
        
        # App icon square
        ix = 28
        draw.rounded_rectangle([ix, y + 11, ix + 36, y + 47], radius=10, fill=SURFACE_LOW)
        draw.text((ix + 12, y + 18), label[0], fill=COLOR_PRIMARY, font=font_title_sm)
        
        # Titles
        draw.text((74, y + 10), label, fill=TEXT_HIGH, font=font_body_bold)
        draw.text((74, y + 28), f"{pkg} • {pss}", fill=TEXT_LOW, font=font_label_sm)
        
        # Pill Tag on right
        bbox = draw.textbbox((0, 0), tag, font=font_label_sm)
        tw = bbox[2] - bbox[0]
        pw = tw + 16
        px = 16 + card_w - pw - 12
        draw.rounded_rectangle([px, y + 17, px + pw, y + 39], radius=10, fill=SURFACE_LOW)
        draw.text((px + 8, y + 22), tag, fill=tag_col, font=font_label_sm)
        
        y += app_h + 8
        
    draw_bottom_nav(draw, PHONE_WIDTH, PHONE_HEIGHT, selected_idx=4)
    draw_device_frame(draw, PHONE_WIDTH, PHONE_HEIGHT)
    return im


# ==========================================
# 6. TABLET ADAPTIVE SCREEN
# ==========================================
def render_tablet_adaptive():
    w, h = TABLET_WIDTH, TABLET_HEIGHT
    im = Image.new("RGB", (w, h), BG_COLOR)
    draw = ImageDraw.Draw(im)
    
    # Left Navigation Rail (M3 Expressive)
    rail_w = 96
    draw.rectangle([0, 0, rail_w, h], fill=SURFACE_LOW)
    draw.line([rail_w, 0, rail_w, h], fill=(42, 46, 54), width=1)
    
    # App Logo on top of Rail
    cx, cy, cr = rail_w / 2, 44, 20
    cookie_pts = []
    for i in range(24):
        ang = i * (2 * math.pi / 24)
        r = cr if i % 2 == 0 else cr * 0.82
        cookie_pts.append((cx + r * math.cos(ang), cy + r * math.sin(ang)))
    draw.polygon(cookie_pts, fill=COLOR_PRIMARY_CONTAINER)
    draw.arc([cx - 9, cy - 9, cx + 9, cy + 9], 150, 390, fill=COLOR_PRIMARY, width=2)
    draw.line([cx, cy, cx + 5, cy - 5], fill=COLOR_PRIMARY, width=2)
    
    # Nav Rail Items
    items = ["Overview", "CPU", "GPU", "RAM", "Processes"]
    ny = 100
    for idx, item in enumerate(items):
        is_sel = (idx == 0)
        iy = ny + idx * 72
        
        if is_sel:
            draw.rounded_rectangle([rail_w/2 - 28, iy, rail_w/2 + 28, iy + 34], radius=17, fill=COLOR_PRIMARY_CONTAINER)
            
        col = COLOR_ON_PRIMARY_CONTAINER if is_sel else TEXT_MED
        # Small icon
        ic_y = iy + 10
        draw.rectangle([rail_w/2 - 7, ic_y - 6, rail_w/2 - 1, ic_y], outline=col, width=1)
        draw.rectangle([rail_w/2 + 1, ic_y - 6, rail_w/2 + 7, ic_y + 4], outline=col, width=1)
        draw.rectangle([rail_w/2 - 7, ic_y + 3, rail_w/2 - 1, ic_y + 7], outline=col, width=1)
        
        font = font_label if is_sel else font_label_sm
        bbox = draw.textbbox((0, 0), item, font=font)
        tw = bbox[2] - bbox[0]
        draw.text((rail_w/2 - tw/2, iy + 40), item, fill=COLOR_PRIMARY if is_sel else TEXT_MED, font=font)
        
    # Top Status & App Bar in Main Content Area
    content_x = rail_w + 24
    content_w = w - content_x - 24
    
    # Status on top
    draw.text((content_x, 14), "10:42 • Connected via Wi-Fi • 98% Battery", fill=TEXT_MED, font=font_label_sm)
    
    # App Bar
    ay = 36
    draw.text((content_x, ay), "PerfPulse Adaptive Dashboard", fill=TEXT_HIGH, font=font_title_lg)
    draw.text((content_x, ay + 24), "Large Screen • NavigationSuiteScaffold Multi-Column Layout", fill=COLOR_PRIMARY, font=font_label)
    
    # Action buttons on right
    rw, rh = 68, 28
    rx = w - rw - 24
    draw.rounded_rectangle([rx, ay + 4, rx + rw, ay + 4 + rh], radius=14, fill=SURFACE_HIGH, outline=OUTLINE_VARIANT)
    draw.text((rx + 11, ay + 10), "1000ms", fill=TEXT_HIGH, font=font_label)
    
    # 3-Column Grid Layout
    grid_y = 86
    col_width = (content_w - 32) / 3
    col1_x = content_x
    col2_x = col1_x + col_width + 16
    col3_x = col2_x + col_width + 16
    
    # ---- COLUMN 1: Hardware Gauges ----
    draw.text((col1_x, grid_y), "Hardware Units", fill=TEXT_HIGH, font=font_title_sm)
    cy = grid_y + 24
    draw_gauge_card(
        draw, col1_x, cy, col_width, 100,
        title="CPU", subtitle="Google Tensor G4",
        percent=42.0, detail="8/8 Cores Online • schedutil",
        shape_type="cookie", color=COLOR_PRIMARY, shape_bg=COLOR_PRIMARY_CONTAINER
    )
    cy += 114
    draw_gauge_card(
        draw, col1_x, cy, col_width, 100,
        title="GPU", subtitle="Mali-G715 Immortalis",
        percent=35.0, detail="700 MHz • /sys/kernel/gpu",
        shape_type="sunny", color=COLOR_TERTIARY, shape_bg=COLOR_TERTIARY_CONTAINER
    )
    cy += 114
    draw_gauge_card(
        draw, col1_x, cy, col_width, 100,
        title="RAM", subtitle="7.8 GB / 15.4 GB Used",
        percent=51.0, detail="Swap/ZRAM: 1.2 GB • Free: 7.6 GB",
        shape_type="clover", color=COLOR_SECONDARY, shape_bg=COLOR_SECONDARY_CONTAINER
    )
    cy += 114
    # Thermal Zone Tile
    draw.rounded_rectangle([col1_x, cy, col1_x + col_width, cy + 96], radius=20, fill=SURFACE_HIGH)
    draw.text((col1_x + 16, cy + 14), "Thermal Status", fill=TEXT_HIGH, font=font_body_bold)
    draw.text((col1_x + 16, cy + 34), "Battery: 34.2 °C • Headroom: Optimal", fill=STATUS_GREEN, font=font_label_sm)
    draw.text((col1_x + 16, cy + 54), "GPU Diode: 38.5 °C • CPU: 41.2 °C", fill=TEXT_MED, font=font_label_sm)
    draw.text((col1_x + 16, cy + 72), "Thermal Throttling: INACTIVE", fill=TEXT_LOW, font=font_label_sm)
    
    # ---- COLUMN 2: Real-time Telemetry Sparklines ----
    draw.text((col2_x, grid_y), "Real-time Telemetry", fill=TEXT_HIGH, font=font_title_sm)
    card2_y = grid_y + 24
    card2_h = 444
    draw.rounded_rectangle([col2_x, card2_y, col2_x + col_width, card2_y + card2_h], radius=22, fill=SURFACE_HIGH)
    
    draw.text((col2_x + 18, card2_y + 16), "Multi-Stream Rolling History", fill=TEXT_HIGH, font=font_title_sm)
    draw.text((col2_x + 18, card2_y + 34), "Sample rate: 1000ms • Buffer: 60s", fill=TEXT_LOW, font=font_label_sm)
    
    sy = card2_y + 60
    draw.text((col2_x + 18, sy), "CPU Load: 42.0%", fill=COLOR_PRIMARY, font=font_body_bold)
    cpu_data = [18, 22, 28, 35, 48, 52, 45, 38, 42, 50, 62, 55, 40, 36, 42, 45, 39, 42]
    draw_sparkline(draw, col2_x + 18, sy + 18, col_width - 36, 68, cpu_data, COLOR_PRIMARY, (15, 45, 65, 80))
    
    sy += 114
    draw.text((col2_x + 18, sy), "GPU Load: 35.0%", fill=COLOR_TERTIARY, font=font_body_bold)
    gpu_data = [10, 15, 20, 25, 45, 60, 52, 30, 22, 28, 35, 40, 48, 32, 28, 35]
    draw_sparkline(draw, col2_x + 18, sy + 18, col_width - 36, 68, gpu_data, COLOR_TERTIARY, (45, 25, 55, 80))
    
    sy += 114
    draw.text((col2_x + 18, sy), "RAM Usage: 51.0%", fill=COLOR_SECONDARY, font=font_body_bold)
    ram_data = [49, 49, 50, 50, 50, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51]
    draw_sparkline(draw, col2_x + 18, sy + 18, col_width - 36, 68, ram_data, COLOR_SECONDARY, (35, 45, 55, 80))
    
    # ---- COLUMN 3: Tri-Cluster Cores & Process Telemetry ----
    draw.text((col3_x, grid_y), "Cores & Processes", fill=TEXT_HIGH, font=font_title_sm)
    cy3 = grid_y + 24
    
    # Cores Card
    core_card_h = 230
    draw.rounded_rectangle([col3_x, cy3, col3_x + col_width, cy3 + core_card_h], radius=22, fill=SURFACE_HIGH)
    draw.text((col3_x + 18, cy3 + 14), "Active CPU Clusters", fill=TEXT_HIGH, font=font_title_sm)
    
    sample_cores = [
        ("Core 0 (LITTLE)", "1320 MHz", 32.0),
        ("Core 1 (LITTLE)", "1440 MHz", 41.0),
        ("Core 4 (MID)", "2100 MHz", 55.0),
        ("Core 7 (BIG/PRIME)", "2850 MHz", 84.0),
    ]
    
    py = cy3 + 42
    for cname, freq, pct in sample_cores:
        draw.text((col3_x + 18, py), cname, fill=TEXT_HIGH, font=font_label_sm)
        draw.text((col3_x + col_width - 70, py), freq, fill=COLOR_PRIMARY, font=font_label_sm)
        draw_wavy_linear_bar(draw, col3_x + 18, py + 18, col_width - 36, 8, pct, COLOR_PRIMARY, amp=1.5)
        py += 44
        
    cy3 += core_card_h + 14
    
    # App Processes Card
    proc_card_h = 200
    draw.rounded_rectangle([col3_x, cy3, col3_x + col_width, cy3 + proc_card_h], radius=22, fill=SURFACE_HIGH)
    draw.text((col3_x + 18, cy3 + 14), "Top Process Telemetry", fill=TEXT_HIGH, font=font_title_sm)
    
    proc_samples = [
        ("Google Chrome", "320 MB PSS", "Foreground"),
        ("Spotify", "185 MB PSS", "Audio Service"),
        ("System UI", "142 MB PSS", "System Compositor"),
        ("PerfPulse Monitor", "46.2 MB PSS", "14 Threads • Active"),
    ]
    
    py = cy3 + 42
    for pname, mem, role in proc_samples:
        draw.text((col3_x + 18, py), pname, fill=TEXT_HIGH, font=font_body_bold)
        draw.text((col3_x + 18, py + 16), f"{mem} • {role}", fill=TEXT_MED, font=font_label_sm)
        py += 38
        
    draw_device_frame(draw, w, h, radius=24)
    return im


if __name__ == "__main__":
    script_dir = os.path.dirname(os.path.abspath(__file__))
    out_dir = os.path.abspath(os.path.join(script_dir, "..", "screenshots"))
    os.makedirs(out_dir, exist_ok=True)
    
    print("Generating 01_overview.png...")
    im_overview = render_overview()
    im_overview.save(os.path.join(out_dir, "01_overview.png"), "PNG")
    
    print("Generating 02_cpu.png...")
    im_cpu = render_cpu()
    im_cpu.save(os.path.join(out_dir, "02_cpu.png"), "PNG")
    
    print("Generating 03_gpu.png...")
    im_gpu = render_gpu()
    im_gpu.save(os.path.join(out_dir, "03_gpu.png"), "PNG")
    
    print("Generating 04_ram.png...")
    im_ram = render_ram()
    im_ram.save(os.path.join(out_dir, "04_ram.png"), "PNG")
    
    print("Generating 05_processes.png...")
    im_proc = render_processes()
    im_proc.save(os.path.join(out_dir, "05_processes.png"), "PNG")
    
    print("Generating 06_tablet_adaptive.png...")
    im_tab = render_tablet_adaptive()
    im_tab.save(os.path.join(out_dir, "06_tablet_adaptive.png"), "PNG")
    
    print("All 6 screenshots generated successfully!")
