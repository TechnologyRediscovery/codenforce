"""
Creates
"""





def main():
    parcel_ids = ask_database_for_parcelids()
    for parid in parcel_ids:
        raw_html = scrape_county_prop_assesment()
        parsed_data = extract_info_from_html(raw_html)
        written_data = write_data_to_propertyexternaldata(parsed_data)
        did_the_data_change = check_if_data_is_different_than_previous(written_data)
        if did_the_data_change:
            create_event


if __name__ == '__main__':
    main()