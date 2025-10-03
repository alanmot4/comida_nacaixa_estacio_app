import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ShoppingCart } from "lucide-react";

interface MarmitaCardProps {
  id: string;
  name: string;
  description: string;
  price: number;
  image_url?: string;
  onAddToCart: () => void;
}

export const MarmitaCard = ({
  name,
  description,
  price,
  image_url,
  onAddToCart,
}: MarmitaCardProps) => {
  return (
    <Card className="overflow-hidden transition-all duration-300 hover:shadow-[var(--shadow-hover)] group">
      <div className="relative h-48 overflow-hidden">
        {image_url && (
          <img
            src={image_url}
            alt={name}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110"
          />
        )}
      </div>
      <div className="p-6">
        <h3 className="text-xl font-bold mb-2 text-foreground">{name}</h3>
        <p className="text-muted-foreground mb-4 line-clamp-2">{description}</p>
        <div className="flex items-center justify-between">
          <span className="text-2xl font-bold text-primary">
            R$ {price.toFixed(2)}
          </span>
          <Button onClick={onAddToCart} size="default" className="gap-2">
            <ShoppingCart className="w-4 h-4" />
            Adicionar
          </Button>
        </div>
      </div>
    </Card>
  );
};
