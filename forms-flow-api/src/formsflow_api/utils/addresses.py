import re
from flask import current_app

ROAD_TYPES = {
    'булевард': 'бул.',
    'улица': 'ул.',
    'площад': 'пл.',
    'квартал': 'кв.',
    'жилищенкомплекс': 'ж.к.',
    "бул.": "бул.",
    "ул.": "ул.",
    "пл.": "пл.",
    "кв.": "кв.",
    "ж.к.": "ж.к.",
}

ROAD_TYPES_VID_PA = {
    'бул.': 2,
    'ул.': 3,
    'пл.': 1,
    'кв.': 5,
    'ж.к': 4,
}


def replace_road_types(name_pa: str):
    # Split by spaces and convert to lowercase
    name_pa_list = name_pa.lower().split()

    # Create a set to track which road types have been found
    found_road_types = set()
    vid_pa_values = []

    # Perform regex replace for each road type
    for road_type, abbreviation in ROAD_TYPES.items():
        # pattern = re.compile(r'\b' + re.escape(road_type) + r'\b', re.IGNORECASE)
        pattern = re.compile(r'(?<!\w)' + re.escape(road_type) + r'(?!\w)', re.IGNORECASE)
        matches = [word for word in name_pa_list if pattern.fullmatch(word)]

        if matches:
            # If road type is found, add to the set
            found_road_types.add(road_type)
            # Remove all occurrences of the road type
            name_pa_list = [word for word in name_pa_list if not pattern.fullmatch(word)]

    # Prepend the abbreviations of found road types
    for road_type in found_road_types:
        name_pa_list.insert(0, ROAD_TYPES[road_type])

    # Join the list back into a string
    if found_road_types:
        name_pa = ''.join(name_pa_list[:1]) + ' '.join(name_pa_list[1:])
    else:
        name_pa = ' '.join(name_pa_list)

    for road_type, vid_pa in ROAD_TYPES_VID_PA.items():
        if road_type in found_road_types:
            vid_pa_values.append(vid_pa)

    current_app.logger.debug(name_pa)
    current_app.logger.debug(vid_pa_values)

    return name_pa, vid_pa_values
