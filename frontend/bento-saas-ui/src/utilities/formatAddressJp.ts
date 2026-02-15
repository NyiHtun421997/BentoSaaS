import type { AddressDto } from "../lib/api/types";

type AddressLike =
  | Pick<
      AddressDto,
      | "postalCode"
      | "prefecture"
      | "city"
      | "district"
      | "chomeBanGo"
      | "buildingNameRoomNo"
    >
  | null
  | undefined;

const compact = (parts: Array<string | null | undefined>) =>
  parts
    .map((part) => part?.trim())
    .filter((part): part is string => Boolean(part && part.length > 0));

export default function formatAddressJp(
  address: AddressLike,
  includePostal = true,
): string {
  if (!address) return "";

  const body = compact([
    address.prefecture,
    address.city,
    address.district,
    address.chomeBanGo,
    address.buildingNameRoomNo,
  ]).join(" ");

  const postal = address.postalCode?.trim();

  if (includePostal) {
    if (postal && body) return `〒 ${postal} ${body}`;
    if (postal) return `〒 ${postal}`;
  }
  return body;
}
